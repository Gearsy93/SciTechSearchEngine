package com.gearsy.scitechsearchengine.service

import com.gearsy.scitechsearchengine.config.properties.Neo4jProperties
import com.gearsy.scitechsearchengine.model.CSCSTIRubricatorEmbeddedNode
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.sqrt

@Service
class RelevantRubricSearchService(
    neo4jProperties: Neo4jProperties,
    private val embeddingProcessService: EmbeddingProcessService
) {

    private val logger = LoggerFactory.getLogger(RelevantRubricSearchService::class.java)

    private var driver: Driver = GraphDatabase.driver(
        neo4jProperties.uri, AuthTokens.basic(neo4jProperties.username, neo4jProperties.password)
    )

    fun getQueryRelevantCSCSTIRubricList(query: String): List<CSCSTIRubricatorEmbeddedNode> {
        logger.info("Поиск релевантных рубрик для запроса: $query")

        // 1. Генерируем эмбеддинг запроса
        val queryEmbedding = embeddingProcessService.generateEmbeddings(listOf(query)).first()

        // 2. Извлекаем все рубрики из Neo4j с их эмбеддингами
        val rubricNodes = mutableListOf<CSCSTIRubricatorEmbeddedNode>()
        driver.session().use { session ->
            val result = session.run("MATCH (r:Rubric) RETURN r.cipher, r.title, r.embedding")
            while (result.hasNext()) {
                val record = result.next()
                val cipher = record["r.cipher"].asString()
                val title = record["r.title"].asString()
                val embedding = record["r.embedding"].asList { (it as org.neo4j.driver.Value).asFloat() }


                rubricNodes.add(CSCSTIRubricatorEmbeddedNode(cipher, title, emptyList(), embedding, mutableListOf()))
            }
        }

        // 3. Вычисляем косинусное сходство с каждой рубрикой
        val rubricSimilarities = rubricNodes.map { rubric ->
            val similarity = cosineSimilarity(queryEmbedding, rubric.embedding)
            rubric to similarity
        }.sortedByDescending { it.second }

        // 4. Постепенно формируем центроид из рубрик, пока схожесть не начнет уменьшаться
        val relevantRubrics = mutableListOf<CSCSTIRubricatorEmbeddedNode>()
        var centroidEmbedding = queryEmbedding.toFloatArray()
        for ((rubric, similarity) in rubricSimilarities) {
            val newCentroid = computeCentroid(centroidEmbedding, rubric.embedding)
            val newSimilarity = cosineSimilarity(queryEmbedding, newCentroid.toList())
            if (newSimilarity < similarity) break
            relevantRubrics.add(rubric)
            centroidEmbedding = newCentroid
        }

        return relevantRubrics
    }

    private fun cosineSimilarity(vec1: List<Float>, vec2: List<Float>): Float {
        val dotProduct = vec1.zip(vec2).sumOf { (a, b) -> (a * b).toDouble() }.toFloat()
        val normVec1 = sqrt(vec1.sumOf { (it * it).toDouble() }.toFloat())
        val normVec2 = sqrt(vec2.sumOf { (it * it).toDouble() }.toFloat())
        return if (normVec1 == 0f || normVec2 == 0f) 0f else dotProduct / (normVec1 * normVec2)
    }

    private fun computeCentroid(vec1: FloatArray, vec2: List<Float>): FloatArray {
        val centroid = FloatArray(vec1.size) { i -> (vec1[i] + vec2[i]) / 2 }
        return centroid
    }
}
