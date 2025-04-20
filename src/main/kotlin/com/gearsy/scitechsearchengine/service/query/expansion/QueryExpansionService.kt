package com.gearsy.scitechsearchengine.service.query.expansion

import com.gearsy.scitechsearchengine.config.properties.QueryExpansionProperties
import com.gearsy.scitechsearchengine.db.neo4j.entity.RubricNode
import com.gearsy.scitechsearchengine.db.neo4j.repository.ContextRubricRepository
import com.gearsy.scitechsearchengine.db.neo4j.repository.ContextTermClientRepository
import com.gearsy.scitechsearchengine.model.yandex.RelevantRubric
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.ln

@Service
class QueryExpansionService(
    private val queryExpansionProperties: QueryExpansionProperties,
    private val contextRubricRepository: ContextRubricRepository,
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


//    fun preparePrescriptionList(queryText: String, rubricNodeList: List<RubricNode>) {
//
//        // Подготовка к генерации поисковых предписаний
//        val relevantRubricsForQuery = rubricNodeList.map { rubricNode ->
//            val relevant = relevantTerms
//                .filter { it.content in (rubricNode.termList?.map { t -> t.content } ?: emptyList()) }
//
//            RelevantRubric(
//                cipher = rubricNode.cipher,
//                title = rubricNode.title,
//                relevantTerms = relevant.map {
//                    RelevantTerm(
//                        content = it.content,
//                        similarity = Vector.of(*it.embedding.toDoubleArray()).cosineSimilarity(queryEmbedding)
//                    )
//                }
//            )
//        }
//
//        // Генерация поисковых предписаний
//        val searchQueries = buildPrescriptions(queryText, selectedRubricsForQuery)
//
//        // Лог или возврат поисковых запросов
//        searchQueries.forEachIndexed { idx, queryLocal ->
//            logger.info("Поисковое предписание #${idx + 1}: $queryLocal")
//        }
//
//
//    }

    fun buildPrescriptions(
        originalQuery: String,
        relevantRubrics: List<RelevantRubric>,
        maxTermsPerQuery: Int = queryExpansionProperties.maxTermsPerQuery.toInt(),
        maxCharsPerQuery: Int = queryExpansionProperties.maxCharsPerQuery.toInt(),
    ): List<String> {
        val result = mutableListOf<String>()

        // Сначала по одному наиболее релевантному термину из каждой рубрики
        val balancedTerms = relevantRubrics
            .mapNotNull { it.relevantTerms.maxByOrNull { term -> term.similarity } }
            .distinctBy { it.content }

        // Затем глобальные термины по убыванию similarity
        val globalTerms = relevantRubrics
            .flatMap { it.relevantTerms }
            .distinctBy { it.content }
            .sortedByDescending { it.similarity }

        // Объединённый список терминов без повторов
        val allTerms = (balancedTerms + globalTerms)
            .distinctBy { it.content }

        // Формируем поисковые запросы с учетом ограничений
        var currentQuery = StringBuilder(originalQuery)
        var currentTermCount = 0

        for (term in allTerms) {
            val formatted = term.content
            val next = " | $formatted"
            if (currentTermCount + 1 > maxTermsPerQuery || currentQuery.length + next.length > maxCharsPerQuery) {
                result.add(currentQuery.toString())
                currentQuery = StringBuilder(originalQuery).append(" | $formatted")
                currentTermCount = 1
            } else {
                currentQuery.append(next)
                currentTermCount++
            }
        }

        if (currentQuery.isNotEmpty() && !result.contains(currentQuery.toString())) {
            result.add(currentQuery.toString())
        }

        return result
    }
}