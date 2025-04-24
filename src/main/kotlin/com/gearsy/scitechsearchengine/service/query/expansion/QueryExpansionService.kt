package com.gearsy.scitechsearchengine.service.query.expansion

import com.gearsy.scitechsearchengine.config.properties.QueryExpansionProperties
import com.gearsy.scitechsearchengine.db.neo4j.entity.RubricNode
import com.gearsy.scitechsearchengine.db.neo4j.entity.TermSourceType
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import com.gearsy.scitechsearchengine.db.neo4j.repository.ContextTermClientRepository
import com.gearsy.scitechsearchengine.model.yandex.PrescriptionTerm
import com.gearsy.scitechsearchengine.model.yandex.SearchPrescription
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import kotlin.math.ln

@Service
class QueryExpansionService(
    private val queryExpansionProperties: QueryExpansionProperties,
    private val contextTermClientRepository: ContextTermClientRepository
) {

    private val logger = LoggerFactory.getLogger(QueryExpansionService::class.java)

    fun evaluateTermListFinalScore(
        sessionId: Long,
        iterativeRubricTermList: List<RubricNode>,
        extendedRubricTermList: List<RubricNode>
    ): List<RubricNode> {

        // Получение данных контекстного тезауруса
        val contextData = contextTermClientRepository.findRubricsWithTermsBySession(sessionId)

        // Построение мапы: "cipher::content" → count
        val contextMap: Map<String, Int> = contextData
            .flatMap { rubric ->
                rubric.termList.map { term ->
                    "${rubric.cipher}::${term.content}" to term.count
                }
            }
            .toMap()

        // Объединение всех рубрик
        val allRubrics = (iterativeRubricTermList + extendedRubricTermList)

        // Пересчёт весов с учетом контекстного тезауруса
        val updatedRubrics = allRubrics.map { rubric ->
            val updatedTerms = rubric.termList?.map { term ->
                val key = "${rubric.cipher}::${term.content}"
                val count = contextMap[key] ?: 0
                val boost = if (count > 0) ln(1.0 + count.toDouble()) else 1.0
                term.copy(score = (term.score ?: 0.0) * boost)
            } ?: emptyList()
            rubric.copy(termList = updatedTerms)
        }

        return updatedRubrics
    }

    fun buildBalancedSearchPrescriptions(
        queryText: String,
        rubrics: List<RubricNode>,
        maxTermsPerQuery: Int = queryExpansionProperties.maxTermsPerQuery.toInt(),
        maxCharsPerQuery: Int = queryExpansionProperties.maxCharsPerQuery.toInt(),
    ): List<SearchPrescription> {
        val rubricStacks: Map<String, Deque<PrescriptionTerm>> = prepareRubricStacks(rubrics)
        val totalTermCount = rubricStacks.values.sumOf { it.size }

        for (k in 1..totalTermCount) {
            val attempt = attemptToBuildPrescriptions(
                queryText,
                rubricStacks.mapValues { ArrayDeque(it.value) },
                k,
                maxTermsPerQuery,
                maxCharsPerQuery
            )
            return attempt
        }

        return emptyList()
    }

    private fun prepareRubricStacks(rubrics: List<RubricNode>): Map<String, Deque<PrescriptionTerm>> {
        return rubrics.associate { rubric ->
            val terms = rubric.termList.orEmpty()

            rubric.cipher to rubric.termList.orEmpty()
                .sortedByDescending { it.score ?: 0.0 }
                .map { term ->
                    PrescriptionTerm(
                        content = term.content,
                        weight = term.score ?: 0.0,
                        sourceType = term.sourceType ?: TermSourceType.GRNTI,
                        thesaurusType = term.thesaurusType ?: ThesaurusType.ITERATIVE,
                        embedding = term.embedding,
                        rubricCipher = rubric.cipher
                    )
                }
                .toCollection(ArrayDeque())
        }
    }


    private fun attemptToBuildPrescriptions(
        queryText: String,
        rubricStacks: Map<String, Deque<PrescriptionTerm>>,
        k: Int,
        maxTermsPerQuery: Int,
        maxCharsPerQuery: Int
    ): List<SearchPrescription> {
        val builders = MutableList(k) { mutableListOf<PrescriptionTerm>() }
        val usedRubricsPerBuilder = List(k) { mutableSetOf<String>() }
        val usedTerms = mutableSetOf<String>()
        val initialTotal = rubricStacks.values.sumOf { it.size }

        // Первичный проход
        rubricStacks.forEach { (cipher, stack) ->
            for (i in 0 until k) {
                if (stack.isNotEmpty()) {
                    val term = stack.removeFirst()
                    val testList = builders[i] + term
                    val text = testList.joinToString(" | ", prefix = "filetype:pdf $queryText |") { it.content }

                    if (term.content !in usedTerms &&
                        cipher !in usedRubricsPerBuilder[i] &&
                        testList.size <= maxTermsPerQuery &&
                        text.length <= maxCharsPerQuery
                    ) {
                        builders[i].add(term)
                        usedRubricsPerBuilder[i].add(cipher)
                        usedTerms.add(term.content)
                        break
                    } else {
                        stack.addFirst(term)
                    }
                }
            }
        }

        // Дозаполнение
        var filledSomething: Boolean
        do {
            filledSomething = false
            for ((cipher, stack) in rubricStacks) {
                if (stack.isNotEmpty()) {
                    val term = stack.first()
                    for (i in 0 until k) {
                        val testList = builders[i] + term
                        val text = testList.joinToString(" | ", prefix = "filetype:pdf $queryText: ") { it.content }

                        if (term.content !in usedTerms &&
                            cipher !in usedRubricsPerBuilder[i] &&
                            testList.size <= maxTermsPerQuery &&
                            text.length <= maxCharsPerQuery
                        ) {
                            builders[i].add(term)
                            usedRubricsPerBuilder[i].add(cipher)
                            usedTerms.add(term.content)
                            stack.removeFirst()
                            filledSomething = true
                            break
                        }
                    }
                }
            }
        } while (filledSomething)

        val placed = usedTerms.size
        if (placed < initialTotal) {

            val overflowTerms = rubricStacks.values.flatten()
                .filter { it.content !in usedTerms }
                .sortedByDescending { it.weight }

            val overflowBuilder = mutableListOf<PrescriptionTerm>()
            for (term in overflowTerms) {
                val testList = overflowBuilder + term
                val text = testList.joinToString(" | ", prefix = "filetype:pdf $queryText: ") { it.content }
                if (testList.size <= maxTermsPerQuery && text.length <= maxCharsPerQuery) {
                    overflowBuilder.add(term)
                    usedTerms.add(term.content)
                } else break
            }

            if (overflowBuilder.isNotEmpty()) {
                val ranked = overflowBuilder.mapIndexed { rank, t -> t.copy(rank = rank + 1) }
                builders += ranked.toMutableList()
            }
        }

        return builders.map { terms ->
            val ranked = terms.mapIndexed { rank, t -> t.copy(rank = rank + 1) }
            SearchPrescription(
                queryText = queryText,
                generatedText = ranked.joinToString(" | ", prefix = "filetype:pdf $queryText: ") { it.content },
                terms = ranked
            )
        }
    }
}