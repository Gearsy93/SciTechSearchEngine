package com.gearsy.scitechsearchengine.service

import org.springframework.stereotype.Service

@Service
class TermThesaurusFormService(private val embeddingProcessService: EmbeddingProcessService) {

    fun generateCSCSTIThesaurusVectors(cscstiCipher: String) {
        val texts = listOf("Пример текста", "Антипример текста")

        val embeddings = embeddingProcessService.generateEmbeddings(texts)

        println()
    }
}