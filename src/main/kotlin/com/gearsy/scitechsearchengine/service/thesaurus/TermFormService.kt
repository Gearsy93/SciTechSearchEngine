package com.gearsy.scitechsearchengine.service.thesaurus

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gearsy.scitechsearchengine.controller.dto.embedding.EmbeddingRequestDTO
import com.gearsy.scitechsearchengine.service.lang.model.EmbeddingService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import mikera.vectorz.Vector


@Service
class TermFormService(private val embeddingProcessService: EmbeddingService) {

    private val logger = LoggerFactory.getLogger(TermFormService::class.java)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val embeddingCache = object : LinkedHashMap<String, FloatArray>(10000, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, FloatArray>?): Boolean {
            return size > 10000
        }
    }

    fun generateCSCSTIThesaurusVectors(cscstiCipher: String) {
        logger.info("Загрузка JSON для рубрики $cscstiCipher")
        val cscstiFilePath = "src/main/resources/rubricator/cscstiEnrich/$cscstiCipher.json"
        val factory = objectMapper.factory
        val parser = factory.createParser(File(cscstiFilePath))

        val cscstiJsonNode: JsonNode = objectMapper.readTree(parser)

        val updatedJson = processRubric(cscstiJsonNode)


        val outputPath = "src/main/resources/rubricator/cscstiEmbeddings/$cscstiCipher.json"
        objectMapper.writeValue(File(outputPath), updatedJson)
        logger.info("Файл с эмбеддингами сохранен: $outputPath")
    }

    private fun processRubric(node: JsonNode): JsonNode {
        val updatedNode = objectMapper.createObjectNode()
        val cipher = node.get("cipher")?.asText() ?: ""
        val title = node.get("title")?.asText() ?: ""

        logger.info("Обработка рубрики: $title ($cipher)")

        // Извлекается содержимое ключевых слов, фильтруются дубликаты, максимум 3000 слов
        val termList = node.get("termList")
            ?.mapNotNull { it.get("content")?.asText() }
            ?.distinct()
            ?.take(3000)
            ?: emptyList()



        val termEmbeddings = runBlocking { generateTermEmbeddings(termList, title) }
        val childNodes = node.get("children")?.map { processRubric(it) } ?: emptyList()

        val rubricEmbedding = if (childNodes.isEmpty()) {
            runBlocking {computeSentenceEmbedding(termList, title)}
        } else {
            computeCentroidEmbedding(termList, childNodes, title)
        }

        updatedNode.put("cipher", cipher)
        updatedNode.put("title", title)
        updatedNode.set<ArrayNode>("termList", objectMapper.valueToTree(termEmbeddings))
        updatedNode.set<ArrayNode>("embedding", objectMapper.valueToTree(rubricEmbedding))
        updatedNode.set<ArrayNode>("children", objectMapper.valueToTree(childNodes))

        return updatedNode
    }

    private suspend fun generateTermEmbeddings(terms: List<String>, title: String): List<Map<String, Any>> {
        if (terms.isEmpty()) return emptyList()

        val requests = terms.map { term ->
            EmbeddingRequestDTO(
                term = term,
                context = terms.filterNot { it == term },
                title = title
            )
        }

        val chunked = requests.chunked(128)
        val embeddings = mutableListOf<List<Float>>()

        for (chunk in chunked) {
            embeddings += embeddingProcessService.requestBatchEmbeddings(chunk)
        }


        return terms.mapIndexed { index, term ->
            mapOf(
                "content" to term,
                "embedding" to embeddings[index]
            )
        }
    }

    suspend fun computeSentenceEmbedding(terms: List<String>, title: String): FloatArray {
        val maxTerms = 800
        val sentenceKey = (terms + title).take(maxTerms).joinToString(", ")

        return embeddingCache.getOrPut(sentenceKey) {
            runBlocking {
                embeddingProcessService.requestRubricEmbedding(title, terms).toFloatArray()
            }
        }
    }

    private fun computeCentroidEmbedding(
        terms: List<String>,
        children: List<JsonNode>,
        title: String
    ): FloatArray {

        val rubricEmbeddingArray = runBlocking {computeSentenceEmbedding(terms, title) } // FloatArray
        val rubricEmbedding = Vector.of(*rubricEmbeddingArray.map { it.toDouble() }.toDoubleArray())

        // Получаем векторы дочерних рубрик
        val childEmbeddings = children.mapNotNull { child ->
            child.get("embedding")?.let { e ->
                val floatArray = objectMapper.readValue<FloatArray>(e.toString())
                Vector.of(*floatArray.map { it.toDouble() }.toDoubleArray())
            }
        }

        if (childEmbeddings.isEmpty()) {
            return rubricEmbedding.toDoubleArray().map { it.toFloat() }.toFloatArray()
        }

        // Суммируем векторы: сначала рубрику, затем всех потомков
        val sumVector = rubricEmbedding.copy()
        childEmbeddings.forEach { sumVector.add(it) }

        // Вычисляем среднее (центроид)
        val total = (childEmbeddings.size + 1).toDouble()
        val centroidVector = sumVector.divideCopy(total)

        return centroidVector.toDoubleArray().map { it.toFloat() }.toFloatArray()
    }
}