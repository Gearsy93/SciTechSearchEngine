package com.gearsy.scitechsearchengine.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service
import java.io.File

@Service
class TermThesaurusFormService(private val embeddingProcessService: EmbeddingProcessService) {

    fun generateCSCSTIThesaurusVectors(cscstiCipher: String) {
        // Маппер объектов для дереализации
        val objectMapper = jacksonObjectMapper()

        // Загрузка содержимого рубрики ГРНТИ
        val cscstiFilePath = "src/main/resources/output/rubricator/cscsti/${cscstiCipher}.json"
        val cscstiFileContent = File(cscstiFilePath).readText(Charsets.UTF_8)
        val cscstiJsonNode = objectMapper.readTree(cscstiFileContent)

        //        val embeddings = embeddingProcessService.generateEmbeddings(texts)
    }
}