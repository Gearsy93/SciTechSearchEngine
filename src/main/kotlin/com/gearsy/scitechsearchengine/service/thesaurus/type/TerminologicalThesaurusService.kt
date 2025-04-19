package com.gearsy.scitechsearchengine.service.thesaurus.type

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gearsy.scitechsearchengine.config.properties.EmbeddingServiceProperties
import com.gearsy.scitechsearchengine.controller.dto.embedding.EmbeddingRequestDTO
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType
import com.gearsy.scitechsearchengine.model.thesaurus.RubricImportDTO
import com.gearsy.scitechsearchengine.service.lang.model.EmbeddingService
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricDBImportService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import mikera.vectorz.Vector


@Service
class TerminologicalThesaurusService(
    private val rubricDBImportService: RubricDBImportService,
    private val embeddingProcessService: EmbeddingService,
    private val embeddingServiceProperties: EmbeddingServiceProperties
) {
    val mapper = jacksonObjectMapper()
    private val logger = LoggerFactory.getLogger(TerminologicalThesaurusService::class.java)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val embeddingCache: LinkedHashMap<String, FloatArray> = object : LinkedHashMap<String, FloatArray>(10000, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, FloatArray>?): Boolean {
            return size > 10000
        }
    }

    fun fillTermThesaurus(rubricCipher: String) {

        // Путь к файлу JSON
        val rubricEmbeddingsPath = "src/main/resources/rubricator/embedding/$rubricCipher.json"
        val rootRubric: RubricImportDTO = mapper.readValue(File(rubricEmbeddingsPath))

        // Открываем сессию и заполняем базу
        rubricDBImportService.insertRubricsAndTermsHierarchy(rootRubric, ThesaurusType.TERMINOLOGICAL)
    }

    fun generateTermThesaurusEmbeddings(rubricatorCipher: String) {
        logger.info("Загрузка JSON для рубрики $rubricatorCipher")
        val rubricatorPath = "src/main/resources/rubricator/term/$rubricatorCipher.json"
        val factory = objectMapper.factory
        val parser = factory.createParser(File(rubricatorPath))

        val rubricatorJsonNode: JsonNode = objectMapper.readTree(parser)

        val updatedJson = generateRubricTermEmbeddings(rubricatorJsonNode)

        val outputPath = "src/main/resources/rubricator/embedding/$rubricatorCipher.json"
        objectMapper.writeValue(File(outputPath), updatedJson)
        logger.info("Файл с эмбеддингами сохранен: $outputPath")
    }

    private fun generateRubricTermEmbeddings(node: JsonNode): JsonNode {
        val updatedNode = objectMapper.createObjectNode()
        val cipher = node.get("cipher")?.asText() ?: ""
        val title = node.get("title")?.asText() ?: ""

        logger.info("Обработка рубрики: $title ($cipher)")

        // Извлекается содержимое ключевых слов, фильтруются дубликаты, максимум 3000 слов
        val rawTermList = node.get("termList")?.toList() ?: emptyList()

        val termList = rawTermList
            .mapNotNull { term ->
                val content = term.get("content")?.asText()
                val rubricatorId = term.get("rubricatorId")?.asInt()
                if (content != null) {
                    mapOf(
                        "content" to content,
                        "rubricatorId" to rubricatorId
                    )
                } else null
            }
            .distinctBy { it["content"] }
            .take(embeddingServiceProperties.maxEmbeddingTermCount.toInt())

        val termEmbeddings = runBlocking { embeddingProcessService.generateTermListEmbeddings(termList, title) }
        val childNodes = node.get("children")?.map { generateRubricTermEmbeddings(it) } ?: emptyList()

        val rubricEmbedding = if (childNodes.isEmpty()) {
            val contents = termList.map { it["content"] as String }
            runBlocking { computeSentenceEmbedding(contents, title) }
        } else {
            val contents = termList.map { it["content"] as String }
            computeCentroidEmbedding(contents, childNodes, title)
        }


        updatedNode.put("cipher", cipher)
        updatedNode.put("title", title)
        updatedNode.set<ArrayNode>("termList", objectMapper.valueToTree(termEmbeddings))
        updatedNode.set<ArrayNode>("embedding", objectMapper.valueToTree(rubricEmbedding))
        updatedNode.set<ArrayNode>("children", objectMapper.valueToTree(childNodes))

        return updatedNode
    }

    suspend fun computeSentenceEmbedding(termList: List<String>, title: String): FloatArray {
        val sentenceKey = (termList + title).take(embeddingServiceProperties.maxEmbeddingTermCount.toInt()).joinToString(", ")

        return embeddingCache.getOrPut(sentenceKey) {
            runBlocking {
                embeddingProcessService.requestRubricEmbedding(title, termList).toFloatArray()
            }
        }
    }

    private fun computeCentroidEmbedding(
        terms: List<String>,
        children: List<JsonNode>,
        title: String
    ): FloatArray {

        val rubricEmbeddingArray = runBlocking {computeSentenceEmbedding(terms, title) }
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