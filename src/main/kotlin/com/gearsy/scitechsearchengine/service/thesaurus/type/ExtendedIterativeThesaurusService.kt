package com.gearsy.scitechsearchengine.service.thesaurus.type

import com.gearsy.scitechsearchengine.db.neo4j.entity.RubricNode
import com.gearsy.scitechsearchengine.db.neo4j.entity.TermNode
import com.gearsy.scitechsearchengine.db.neo4j.entity.TermSourceType
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import com.gearsy.scitechsearchengine.db.neo4j.repository.RubricClientRepository
import com.gearsy.scitechsearchengine.db.neo4j.repository.RubricNeo4jRepository
import com.gearsy.scitechsearchengine.db.neo4j.repository.TermClientRepository
import com.gearsy.scitechsearchengine.db.neo4j.repository.TermNeo4jRepository
import com.gearsy.scitechsearchengine.model.viniti.catalog.VinitiDocumentMeta
import com.gearsy.scitechsearchengine.service.lang.model.EmbeddingService
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricSearchAlgorithmService
import kotlinx.coroutines.runBlocking
import mikera.vectorz.Vector
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExtendedIterativeThesaurusService(
    private val termRepository: TermNeo4jRepository,
    private val embeddingService: EmbeddingService,
    private val rubricRepository: RubricNeo4jRepository,
    private val relevantRubricTermSearchService: RubricSearchAlgorithmService,
    private val termClientRepository: TermClientRepository,
    private val rubricClientRepository: RubricClientRepository
) {

    private val logger = LoggerFactory.getLogger(ExtendedIterativeThesaurusService::class.java)

    @Transactional
    fun insertStructuredRubricAndTerms(
        queryId: Long,
        sessionId: Long,
        queryText: String,
        thesaurusType: ThesaurusType,
        vinitiSearchResults: List<VinitiDocumentMeta>
    ) {
        // Группировка терминов из VINITI по шифру рубрики
        val groupedTerms: Map<String, Set<String>> = vinitiSearchResults
            .flatMap { it.rubricTermDataList }
            .filter { it.keywords != null }
            .groupBy({ it.rubricCipher }, { it.keywords!! })
            .mapValues { it.value.flatten().toSet() }

        val rubricCiphers = groupedTerms.keys

        // Получаем контекст из Neo4j (все существующие термины для каждой рубрики)
        val groupedContextTerms: Map<String, List<String>> =
            termClientRepository.findAllTermsGroupedByRubric(groupedTerms.keys.map { it.trim() }.toSet()).groupBy(
                { it.cipher },
                { it.content }
            )


        // Получаем названия рубрик
        val rubricTitles = rubricClientRepository.getRubricTitles(rubricCiphers).associate {
            it["cipher"]!! to it["title"]!!
        }

        // Формируем структуру {cipher → List<termMap>}
        val validRubricCiphers = rubricTitles.keys
        val termMapList: Map<String, List<Map<String, Any>>> = groupedTerms
            .filterKeys { it in validRubricCiphers }
            .mapValues { (_, terms) ->
                terms.map { term ->
                    mapOf("content" to term, "rubricatorId" to 3)
                }
            }


        // Получаем эмбеддинги для терминов в контексте существующих терминов из Neo4j
        val allTermsWithEmbeddings: List<Map<String, Any>> = termMapList.flatMap { (cipher, termMaps) ->
            val context = groupedContextTerms[cipher] ?: emptyList()
            val title = rubricTitles[cipher] ?: "Без названия"

            runBlocking {
                embeddingService.generateEmbeddingsWithExternalContext(
                    terms = termMaps,
                    rubricTitle = title,
                    contextTerms = context
                ).map { response ->
                    mapOf(
                        "content" to response.term,
                        "embedding" to response.embedding,
                        "thesaurusType" to thesaurusType.toString(),
                        "sourceType" to TermSourceType.VINITI_CATALOG.toString(),
                        "sessionId" to sessionId,
                        "queryId" to queryId,
                        "cipher" to cipher
                    )
                }
            }
        }

        // Преобразуем List<Map> в Map<String, List<TermNode>>
        val termNodesByRubric: Map<String, List<TermNode>> = allTermsWithEmbeddings
            .groupBy { it["cipher"] as String }
            .mapValues { (_, termMaps) ->
                termMaps.map { termMap ->
                    TermNode(
                        content = termMap["content"] as String,
                        embedding = (termMap["embedding"] as List<*>).map { (it as Number).toDouble() },
                        score = (termMap["score"] as? Number)?.toDouble(),
                        thesaurusType = ThesaurusType.valueOf(termMap["thesaurusType"] as String),
                        sourceType = TermSourceType.valueOf(termMap["sourceType"] as String),
                        sessionId = (termMap["sessionId"] as? Number)?.toLong(),
                        queryId = (termMap["queryId"] as? Number)?.toLong()
                    )
                }
            }

        // Отбор релевантных терминов
        val queryVector = relevantRubricTermSearchService.generateQueryVector(queryText)
        val relevantTerms: List<TermNode> = getRelevantTermsByRubricMap(termNodesByRubric, rubricTitles, queryVector)

        // Создание мапы term → cipher для вставки в БД
        val termToCipherMap = termNodesByRubric.flatMap { (cipher, terms) ->
            terms.map { it to cipher }
        }.toMap()

        val termsToInsert: List<Map<String, Any?>> = relevantTerms.map { term ->
            mapOf(
                "content" to term.content,
                "embedding" to term.embedding,
                "score" to term.score,
                "thesaurusType" to term.thesaurusType?.toString(),
                "sourceType" to term.sourceType?.toString(),
                "sessionId" to term.sessionId,
                "queryId" to term.queryId,
                "cipher" to termToCipherMap[term]
            )
        }

        val extendedRubrics = termToCipherMap.values.distinct().mapNotNull { cipher ->
            rubricTitles[cipher]?.let { title ->
                mapOf(
                    "cipher" to cipher,
                    "title" to title,
                    "parentCipher" to null,
                    "embedding" to List(384) { 0.0 },
                    "thesaurusType" to ThesaurusType.EXTENDED_ITERATIVE.toString(),
                    "sessionId" to sessionId,
                    "queryId" to queryId
                )
            }
        }
        rubricRepository.createRubricHierarchy(extendedRubrics)

        termRepository.createTermLinks(termsToInsert)
        logger.info("Вставлено ${termsToInsert.size} терминов в расширенный тезаурус")
    }

    fun getRelevantTermsByRubricMap(
        rubricTermMap: Map<String, List<TermNode>>,
        rubricTitles: Map<String, String>,
        queryVector: Vector
    ): List<TermNode> {
        val pseudoRubrics = rubricTermMap.map { (cipher, terms) ->
            RubricNode(
                cipher = cipher,
                title = rubricTitles[cipher] ?: "Без названия",
                embedding = List(queryVector.length()) { 0.0 },
                termList = terms,
                thesaurusType = ThesaurusType.EXTENDED_ITERATIVE
            )
        }

        return relevantRubricTermSearchService.getRelevantTermListForRubrics(pseudoRubrics, queryVector)
    }


}