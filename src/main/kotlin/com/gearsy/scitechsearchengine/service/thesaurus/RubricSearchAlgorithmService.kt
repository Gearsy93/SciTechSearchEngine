package com.gearsy.scitechsearchengine.service.thesaurus

import com.gearsy.scitechsearchengine.config.properties.RelevantTermRubricProperties
import com.gearsy.scitechsearchengine.db.neo4j.entity.RubricNode
import com.gearsy.scitechsearchengine.db.neo4j.entity.TermNode
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import com.gearsy.scitechsearchengine.db.neo4j.repository.RubricNeo4jRepository
import com.gearsy.scitechsearchengine.db.neo4j.repository.TermNeo4jRepository
import com.gearsy.scitechsearchengine.model.conveyor.RelevantTerm
import com.gearsy.scitechsearchengine.model.conveyor.SelectedRubric
import com.gearsy.scitechsearchengine.service.lang.model.EmbeddingService
import com.gearsy.scitechsearchengine.utils.Neo4jDriverProvider
import mikera.vectorz.Vector
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RubricSearchAlgorithmService(
    private val rubricRepository: RubricNeo4jRepository,
    private val termRepository: TermNeo4jRepository,
    private val embeddingProcessService: EmbeddingService,
    private val relevantTermRubricProperties: RelevantTermRubricProperties
) {

    private val log = LoggerFactory.getLogger(javaClass)

    fun getRelevantTermListFromTermThesaurus(query: String): List<RubricNode> {

        log.info("Начало обработки запроса: '$query'")
        val queryEmbedding = generateQueryVector(query)

        val allRubrics = loadAllRubricsWithChildren()

        val rootRubrics = getRootRubrics(allRubrics)

        val allRubricPairs = allRubrics.map { it to Vector.of(*it.embedding.toDoubleArray()) }
        val rootRubricPairs = rootRubrics.map { it to Vector.of(*it.embedding.toDoubleArray()) }

        val (selectedRoots, _) = selectRootRubrics(queryEmbedding, rootRubricPairs)

        val selectedRubrics = expandSelectedRubrics(
            queryEmbedding,
            selectedRoots,
            allRubricPairs
        )

        val rubricCiphers = selectedRubrics.map { it.cipher }
        val termsByRubric = loadTermsForRubrics(rubricCiphers)

        // Сбор TermNode в поля selectedRubrics
        val fullRubrics = selectedRubrics.map { embedded ->
            val terms = termsByRubric[embedded.cipher]
            RubricNode(
                cipher = embedded.cipher,
                title = embedded.title,
                embedding = embedded.embedding.map { it },
                termList = terms,
                thesaurusType = ThesaurusType.TERMINOLOGICAL
            )
        }

        val relevantTerms = getRelevantTermListForRubrics(fullRubrics, queryEmbedding)

        // Группировка терминов по рубрике
        val termsByRubricGroup = fullRubrics.associateBy({ it.cipher }) { rubric ->
            val rubricTerms = rubric.termList?.map { it.content }?.toSet() ?: emptySet()
            relevantTerms.filter { it.content in rubricTerms }
        }

        // Сбор результатов с терминами
        val result = selectedRubrics.map { embeddedNode ->
            val terms = termsByRubricGroup[embeddedNode.cipher]?.takeIf { it.isNotEmpty() }

            RubricNode(
                cipher = embeddedNode.cipher,
                title = embeddedNode.title,
                embedding = embeddedNode.embedding,
                termList = terms,
                thesaurusType = ThesaurusType.TERMINOLOGICAL
            )
        }

        log.info("\nФинальное количество релевантных рубрик: ${result.size}\n")

        // Подготовка к генерации поисковых предписаний
        val selectedRubricsForQuery = result.map { rubricNode ->
            val relevant = relevantTerms
                .filter { it.content in (rubricNode.termList?.map { t -> t.content } ?: emptyList()) }

            SelectedRubric(
                cipher = rubricNode.cipher,
                title = rubricNode.title,
                relevantTerms = relevant.map {
                    RelevantTerm(
                        content = it.content,
                        similarity = Vector.of(*it.embedding.toDoubleArray()).cosineSimilarity(queryEmbedding)
                    )
                }
            )
        }
        return result
    }

    fun getRootRubrics(rubrics: List<RubricNode>): List<RubricNode> {
        val allChildCiphers = rubrics
            .flatMap { it.children }
            .map { it.cipher }
            .toSet()

        return rubrics.filter { it.cipher !in allChildCiphers }
    }

    fun calculatePenalty(rubric: RubricNode): Double {
        val cipher = rubric.cipher
        val title = rubric.title.lowercase()

        // Штраф по уровню рубрики (чем выше — тем больше штраф)
        val levelPenalty = when (cipher.count { it == '.' }) {
            0 -> relevantTermRubricProperties.penaltyLevelZero.toDouble()
            1 -> relevantTermRubricProperties.penaltyLevelOne.toDouble()
            else -> relevantTermRubricProperties.penaltyOtherLevel.toDouble()
        }

        // Штраф за ключевые "общие" слова в названии
        val generalTitleWords = listOf("общие вопросы", "введение", "основы", "организация", "деятельность", "информация")
        val titlePenalty = if (generalTitleWords.any { it in title }) relevantTermRubricProperties.penaltyTitle.toDouble() else 0.0

        // Жёсткий ручной штраф за часто всплывающие обобщённые рубрики
        val hardcodedPenalty = when (cipher) {
            "20.15", "20.17" -> relevantTermRubricProperties.penaltyHardcode.toDouble()
            else -> 0.0
        }

        return levelPenalty + titlePenalty + hardcodedPenalty
    }

    private fun generateQueryVector(query: String): Vector =
        Vector.of(*embeddingProcessService.generateEmbeddings(listOf(query))[0].map { it.toDouble() }.toDoubleArray())

    fun loadTermsForRubrics(rubricCiphers: List<String>): Map<String, List<TermNode>> {
        return termRepository.loadTermsFromTermThesaurus(rubricCiphers).groupBy(
            keySelector = { it.getRubricCipher() },
            valueTransform = {
                TermNode(
                    content = it.getContent(),
                    embedding = it.getEmbedding(),
                    type = ThesaurusType.TERMINOLOGICAL
                )
            }
        )
    }

    fun loadAllRubricsWithChildren(): List<RubricNode> {
        val projections = rubricRepository.loadRubricHierarchy()

        val rubricMap = mutableMapOf<String, RubricNode>()
        val childLinks = mutableMapOf<String, List<String>>()

        for (record in projections) {
            val cipher = record.getParentCipher()
            val title = record.getParentTitle()
            val embedding = record.getParentEmbedding()
            val childrenCiphers = record.getChildCiphers()

            rubricMap[cipher] = RubricNode(
                cipher = cipher,
                title = title,
                embedding = embedding,
                termList = null,
                thesaurusType = ThesaurusType.TERMINOLOGICAL
            )

            childLinks[cipher] = childrenCiphers
        }

        for ((parentCipher, childCipherList) in childLinks) {
            val parent = rubricMap[parentCipher]
            if (parent != null) {
                val children = childCipherList.mapNotNull { rubricMap[it] }
                rubricMap[parentCipher] = parent.copy(children = children)
            }
        }

        return rubricMap.values.toList()
    }

    fun Vector.cosineSimilarity(other: Vector): Double {
        val dot = this.dotProduct(other)
        val norm1 = this.magnitude()
        val norm2 = other.magnitude()

        return if (norm1 == 0.0 || norm2 == 0.0) 0.0 else dot / (norm1 * norm2)
    }

    private fun selectRootRubrics(
        queryVector: Vector,
        rootRubrics: List<Pair<RubricNode, Vector>>
    ): Pair<List<RubricNode>, Vector> {
        val selected = mutableListOf<RubricNode>()
        var centroid: Vector? = null

        for ((rubric, embedding) in rootRubrics.sortedByDescending { it.second.cosineSimilarity(queryVector) }) {
            val newCentroidAVector = centroid?.addCopy(embedding) ?: embedding
            val newCentroid = Vector.of(*newCentroidAVector.toDoubleArray())

            val simBefore = centroid?.cosineSimilarity(queryVector) ?: 0.0
            val simAfter = newCentroid.cosineSimilarity(queryVector)

            if (simAfter >= simBefore) {
                selected.add(rubric)
                centroid = newCentroid
                log.info("Добавлена корневая рубрика: ${rubric.cipher} - ${rubric.title} (score improved: $simBefore → $simAfter)")
            } else {
                break
            }
        }


        return selected to (centroid ?: Vector.of(*DoubleArray(queryVector.length())))
    }

    private fun expandSelectedRubrics(
        queryVector: Vector,
        selectedRoots: List<RubricNode>,
        allRubrics: List<Pair<RubricNode, Vector>>,
        penaltyThreshold: Double = 0.5
    ): List<RubricNode> {
        val selected = mutableListOf<RubricNode>()
        var centroid: Vector? = null

        for (root in selectedRoots) {
            val children = root.children
                .mapNotNull { child -> allRubrics.find { it.first.cipher == child.cipher } }
                .filter { pair -> calculatePenalty(pair.first) <= penaltyThreshold }
                .sortedByDescending { pair ->
                    val sim = pair.second.cosineSimilarity(queryVector)
                    val penalty = calculatePenalty(pair.first)
                    sim - penalty
                }

            for ((childRubric, childVec) in children) {
                val newCentroidAVector = centroid?.addCopy(childVec) ?: childVec
                val newCentroid = Vector.of(*newCentroidAVector.toDoubleArray())

                val simBefore = centroid?.cosineSimilarity(queryVector) ?: 0.0
                val simAfter = newCentroid.cosineSimilarity(queryVector)

                if (simAfter >= simBefore) {
                    selected.add(childRubric)
                    centroid = newCentroid
                    log.info("Добавлена рубрика второго уровня: ${childRubric.cipher} - ${childRubric.title} (score improved: $simBefore → $simAfter)")

                    // Поиск потомков третьего уровня
                    val grandchildren = childRubric.children
                        .mapNotNull { grand -> allRubrics.find { it.first.cipher == grand.cipher } }
                        .filter { pair -> calculatePenalty(pair.first) <= penaltyThreshold }
                        .sortedByDescending { pair ->
                            val sim = pair.second.cosineSimilarity(queryVector)
                            val penalty = calculatePenalty(pair.first)
                            sim - penalty
                        }

                    for ((grandRubric, grandVec) in grandchildren) {
                        val newCentroidAVector2 = centroid!!.addCopy(grandVec)
                        val newCentroid2 = Vector.of(*newCentroidAVector2.toDoubleArray())

                        val simBefore2 = centroid.cosineSimilarity(queryVector)
                        val simAfter2 = newCentroid2.cosineSimilarity(queryVector)

                        if (simAfter2 >= simBefore2) {
                            selected.add(grandRubric)
                            centroid = newCentroid2
                            log.info("Добавлена рубрика третьего уровня: ${grandRubric.cipher} - ${grandRubric.title} (score improved: $simBefore2 → $simAfter2)")
                        }
                    }
                }
            }
        }

        return selected
    }


    fun getRelevantTermListForRubrics(
        rubrics: List<RubricNode>,
        queryVector: Vector
    ): List<TermNode> {
        val relevantTermList = mutableListOf<TermNode>()

        for (rubric in rubrics) {
            val terms = rubric.termList ?: continue
            if (terms.isEmpty()) continue

            val scored = terms.map { term ->
                term to Vector.of(*term.embedding.map { it }.toDoubleArray())
            }.map { (term, vector) ->
                term to vector.cosineSimilarity(queryVector)
            }.sortedByDescending { it.second }

            if (scored.isEmpty()) continue

            val selected = mutableListOf<TermNode>()
            var centroid = Vector.of(*scored.first().first.embedding.map { it }.toDoubleArray())
            selected.add(scored.first().first)

            val simImproveThreshold = relevantTermRubricProperties.simImproveThreshold.toDouble()
            val minSimilarity = relevantTermRubricProperties.minSimilarity.toDouble()
            for ((term, _) in scored.drop(1)) {
                val candidate = Vector.of(*term.embedding.map { it }.toDoubleArray())
                val newCentroid = Vector.of(
                    *centroid.addCopy(candidate)
                        .toDoubleArray()
                        .map { it / (selected.size + 1) }
                        .toDoubleArray()
                )

                val simBefore = centroid.cosineSimilarity(queryVector)
                val simAfter = newCentroid.cosineSimilarity(queryVector)

                if (simAfter >= simBefore + simImproveThreshold && simAfter > minSimilarity) {
                    selected.add(term)
                    centroid = newCentroid
                    log.info("Добавлен термин: ${term.content} ($simBefore → $simAfter)")
                } else {
                    log.info("Остановлен отбор: ${term.content} ($simBefore → $simAfter)")
                    break
                }
            }

            relevantTermList.addAll(selected)
        }

        return relevantTermList
    }


}
