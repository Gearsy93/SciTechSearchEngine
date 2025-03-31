package com.gearsy.scitechsearchengine.service.thesaurusProcess

import com.gearsy.scitechsearchengine.model.*
import com.gearsy.scitechsearchengine.service.langModelProcess.EmbeddingProcessService
import mikera.vectorz.Vector
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.stereotype.Service

@Service
class RelevantRubricTermSearchService(
    private val embeddingProcessService: EmbeddingProcessService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var neo4jClient: Neo4jClient

    fun getQueryRelevantCSCSTIRubricList(query: String): List<CSCSTIRubricatorEmbeddedNode> {
        log.info("Начало обработки запроса: '$query'")
        val queryEmbedding = generateQueryVector(query)

        val allRubrics = loadAllRubricsWithChildren()

        val rootRubrics = getRootRubricsFrom(allRubrics)

        val allRubricPairs = allRubrics.map { it to Vector.of(*it.embedding!!.toDoubleArray()) }
        val rootRubricPairs = rootRubrics.map { it to Vector.of(*it.embedding!!.toDoubleArray()) }

        val (selectedRoots, _) = selectRootRubrics(queryEmbedding, rootRubricPairs)

        val selectedRubrics = expandSelectedRubrics(
            queryEmbedding,
            selectedRoots,
            allRubricPairs
        )

        val rubricCiphers = selectedRubrics.map { it.cipher }
        val termsByRubric = loadTermsForRubrics(rubricCiphers)

        // Сбор TermEmbeddingNode в поля selectedRubrics
        val fullRubrics = selectedRubrics.map { embedded ->
            val terms = termsByRubric[embedded.cipher]
            CSCSTIRubricNeo4j(
                cipher = embedded.cipher,
                title = embedded.title,
                embedding = embedded.embedding?.map { it },
                children = null,
                termList = terms
            )
        }

        val relevantTerms = getRelevantTermsForRubrics(fullRubrics, queryEmbedding)

        // Группировка терминов по рубрике
        val termsByRubricGroup = fullRubrics.associateBy({ it.cipher }) { rubric ->
            val rubricTerms = rubric.termList?.map { it.content }?.toSet() ?: emptySet()
            relevantTerms.filter { it.content in rubricTerms }
        }

        // Сбор результатов с терминами
        val result = selectedRubrics.map { embeddedNode ->
            val terms = termsByRubricGroup[embeddedNode.cipher]?.takeIf { it.isNotEmpty() }

            CSCSTIRubricatorEmbeddedNode(
                cipher = embeddedNode.cipher,
                title = embeddedNode.title,
                embedding = embeddedNode.embedding,
                termList = terms
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

        // Генерация поисковых предписаний
        val searchQueries = buildSearchQueries(query, selectedRubricsForQuery)

        // Лог или возврат поисковых запросов
        searchQueries.forEachIndexed { idx, queryLocal ->
            log.info("Поисковое предписание #${idx + 1}: $queryLocal")
        }

        return result
    }

    fun getRootRubricsFrom(rubrics: List<CSCSTIRubricNeo4j>): List<CSCSTIRubricNeo4j> {
        val allChildCiphers = rubrics
            .flatMap { it.children ?: emptyList() }
            .map { it.cipher }
            .toSet()

        return rubrics.filter { it.cipher !in allChildCiphers }
    }

    fun calculatePenalty(rubric: CSCSTIRubricNeo4j): Double {
        val cipher = rubric.cipher
        val title = rubric.title.lowercase()

        // Штраф по уровню рубрики (чем выше — тем больше штраф)
        val levelPenalty = when (cipher.count { it == '.' }) {
            0 -> 0.05  // верхний уровень, например "20"
            1 -> 0.03  // средний уровень
            else -> 0.0
        }

        // Штраф за ключевые "общие" слова в названии
        val generalTitleWords = listOf("общие вопросы", "введение", "основы", "организация", "деятельность", "информация")
        val titlePenalty = if (generalTitleWords.any { it in title }) 0.02 else 0.0

        // Жёсткий ручной штраф за часто всплывающие обобщённые рубрики
        val hardcodedPenalty = when (cipher) {
            "20.15", "20.17" -> 1.0
            else -> 0.0
        }

        return levelPenalty + titlePenalty + hardcodedPenalty
    }

    private fun generateQueryVector(query: String): Vector =
        Vector.of(*embeddingProcessService.generateEmbeddings(listOf(query))[0].map { it.toDouble() }.toDoubleArray())

    fun loadTermsForRubrics(rubricCiphers: List<String>): Map<String, List<TermEmbeddingNode>> {
        val rubricCiphersLiteral = rubricCiphers.joinToString(prefix = "[\"", separator = "\", \"", postfix = "\"]")

        val query = """
                        MATCH (t:Term)-[:BELONGS_TO]->(r:Rubric)
                        WHERE r.cipher IN $rubricCiphersLiteral
                        RETURN r.cipher AS rubricCipher, t.content AS content, t.embedding AS embedding
                    """.trimIndent()

        println("Cypher-запрос:\n$query")

        return neo4jClient.query(query)
            .fetch()
            .all()
            .also { result -> println("📦 Получено ${result.size} записей терминов.") }
            .groupBy(
                keySelector = { it["rubricCipher"] as String },
                valueTransform = { record ->
                    TermEmbeddingNode(
                        content = record["content"] as String,
                        embedding = (record["embedding"] as List<*>).map { (it as Number).toDouble() }
                    )
                }
            )

    }

    fun loadAllRubricsWithChildren(): List<CSCSTIRubricNeo4j> {
        val result = neo4jClient.query(
            """
                MATCH (r:Rubric)
                OPTIONAL MATCH (r)-[:HAS_CHILD]->(child:Rubric)
                RETURN
                  r.cipher AS parentCipher,
                  r.title AS parentTitle,
                  r.embedding AS parentEmbedding,
                  collect(child.cipher) AS childCiphers
            """.trimIndent()
        )
        .fetch()
        .all()

        // Создание Map<cipher, CSCSTIRubricNeo4j> без children
        val rubricMap = mutableMapOf<String, CSCSTIRubricNeo4j>()
        val childLinks = mutableMapOf<String, List<String>>() // parentCipher -> list of child ciphers

        for (record in result) {
            val cipher = record["parentCipher"] as String
            val title = record["parentTitle"] as String
            val embedding = (record["parentEmbedding"] as? List<*>)?.map { (it as Number).toDouble() }
            val childrenCiphers = (record["childCiphers"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            rubricMap[cipher] = CSCSTIRubricNeo4j(
                cipher = cipher,
                title = title,
                embedding = embedding,
                children = null,
                termList = null
            )

            childLinks[cipher] = childrenCiphers
        }

        // Присваиваем children по ссылкам из Map
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
        rootRubrics: List<Pair<CSCSTIRubricNeo4j, Vector>>
    ): Pair<List<CSCSTIRubricNeo4j>, Vector> {
        val selected = mutableListOf<CSCSTIRubricNeo4j>()
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
        selectedRoots: List<CSCSTIRubricNeo4j>,
        allRubrics: List<Pair<CSCSTIRubricNeo4j, Vector>>,
        penaltyThreshold: Double = 0.5
    ): List<CSCSTIRubricNeo4j> {
        val selected = mutableListOf<CSCSTIRubricNeo4j>()
        var centroid: Vector? = null

        for (root in selectedRoots) {
            val children = root.children
                ?.mapNotNull { child -> allRubrics.find { it.first.cipher == child.cipher } }
                ?.filter { pair -> calculatePenalty(pair.first) <= penaltyThreshold }
                ?.sortedByDescending { pair ->
                    val sim = pair.second.cosineSimilarity(queryVector)
                    val penalty = calculatePenalty(pair.first)
                    sim - penalty
                } ?: emptyList()

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
                        ?.mapNotNull { grand -> allRubrics.find { it.first.cipher == grand.cipher } }
                        ?.filter { pair -> calculatePenalty(pair.first) <= penaltyThreshold }
                        ?.sortedByDescending { pair ->
                            val sim = pair.second.cosineSimilarity(queryVector)
                            val penalty = calculatePenalty(pair.first)
                            sim - penalty
                        } ?: emptyList()

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


    fun getRelevantTermsForRubrics(
        rubrics: List<CSCSTIRubricNeo4j>,
        queryVector: Vector
    ): List<TermEmbeddingNode> {
        val relevantTerms = mutableListOf<TermEmbeddingNode>()

        for (rubric in rubrics) {
            val terms = rubric.termList ?: continue
            if (terms.isEmpty()) continue

            val scored = terms.map { term ->
                term to Vector.of(*term.embedding.map { it }.toDoubleArray())
            }.map { (term, vector) ->
                term to vector.cosineSimilarity(queryVector)
            }.sortedByDescending { it.second }

            if (scored.isEmpty()) continue

            val selected = mutableListOf<TermEmbeddingNode>()
            var centroid = Vector.of(*scored.first().first.embedding.map { it }.toDoubleArray())
            selected.add(scored.first().first)

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

                if (simAfter >= simBefore) {
                    selected.add(term)
                    centroid = newCentroid
                    log.info("Добавлен термин: ${term.content} ($simBefore → $simAfter)")
                } else {
                    log.info("Остановлен отбор: ${term.content} ($simBefore → $simAfter)")
                    break
                }
            }

            relevantTerms.addAll(selected)
        }

        return relevantTerms
    }

    fun buildSearchQueries(
        originalQuery: String,
        selectedRubrics: List<SelectedRubric>,
        maxTermsPerQuery: Int = 10,
        maxCharsPerQuery: Int = 256
    ): List<String> {
        val result = mutableListOf<String>()

        // Сначала по одному наиболее релевантному термину из каждой рубрики
        val balancedTerms = selectedRubrics
            .mapNotNull { it.relevantTerms.maxByOrNull { term -> term.similarity } }
            .distinctBy { it.content }

        // Затем глобальные термины по убыванию similarity
        val globalTerms = selectedRubrics
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
