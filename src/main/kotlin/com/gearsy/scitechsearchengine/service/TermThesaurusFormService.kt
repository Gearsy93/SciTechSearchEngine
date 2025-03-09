package com.gearsy.scitechsearchengine.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class TermThesaurusFormService(private val embeddingProcessService: EmbeddingProcessService) {

    private val logger = LoggerFactory.getLogger(TermThesaurusFormService::class.java)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
    private val embeddingCache = object : LinkedHashMap<String, FloatArray>(10000, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, FloatArray>?): Boolean {
            return size > 10000  // Ограничиваем кэш до 10,000 записей
        }
    }


    fun generateCSCSTIThesaurusVectors(cscstiCipher: String) {
        logger.info("Загрузка JSON для рубрики $cscstiCipher")
        val cscstiFilePath = "src/main/resources/rubricator/cscstiEnrich/$cscstiCipher.json"
        val factory = objectMapper.factory
        val parser = factory.createParser(File(cscstiFilePath))

        val cscstiJsonNode: JsonNode = objectMapper.readTree(parser) // ✅ Теперь тип явный

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

        val termList = node.get("termList")
            ?.mapNotNull { it.get("content")?.asText() }
            ?.distinct()
            ?.take(3000)  // Ограничиваем список терминов
            ?: emptyList()



        val termEmbeddings = generateTermEmbeddings(termList)
        val childNodes = node.get("children")?.map { processRubric(it) } ?: emptyList()

        val rubricEmbedding = if (childNodes.isEmpty()) {
            computeSentenceEmbedding(termList)
        } else {
            computeCentroidEmbedding(termList, childNodes)
        }

        updatedNode.put("cipher", cipher)
        updatedNode.put("title", title)
        updatedNode.set<ArrayNode>("termList", objectMapper.valueToTree(termEmbeddings))
        updatedNode.set<ArrayNode>("embedding", objectMapper.valueToTree(rubricEmbedding))
        updatedNode.set<ArrayNode>("children", objectMapper.valueToTree(childNodes))

        return updatedNode
    }

    private fun generateTermEmbeddings(terms: List<String>): List<Map<String, Any>> {

        //
        val embeddings = if (terms.isEmpty()) {
            listOf()
        }
        else {
            embeddingProcessService.generateEmbeddings(terms)
        }
        return terms.mapIndexed { index, term ->
            mapOf(
                "content" to term,
                "embedding" to embeddings[index]
            )
        }
    }

    private fun computeSentenceEmbedding(terms: List<String>): FloatArray {
        val maxTerms = 500  // Ограничим длину предложения (количество терминов)
        val sentence = terms.take(maxTerms).joinToString(", ")

        return embeddingCache.getOrPut(sentence) {
            embeddingProcessService.generateEmbeddings(listOf(sentence))[0].toFloatArray()
        }
    }


    private fun computeCentroidEmbedding(terms: List<String>, children: List<JsonNode>): FloatArray {

        // TODO добавить к terms наименование рубрики
        val rubricEmbedding = computeSentenceEmbedding(terms)

        val childEmbeddings = children.mapNotNull {
            it.get("embedding")?.let { e -> objectMapper.readValue<FloatArray>(e.toString()) }
        }

        if (childEmbeddings.isEmpty()) return rubricEmbedding

        val dimension = rubricEmbedding.size
        val centroid = FloatArray(dimension)
        val totalEmbeddings = childEmbeddings.size + 1

        for (i in 0 until dimension) {
            centroid[i] = (rubricEmbedding[i] + childEmbeddings.sumOf { it[i].toDouble() }.toFloat()) / totalEmbeddings
        }

        return centroid
    }

}