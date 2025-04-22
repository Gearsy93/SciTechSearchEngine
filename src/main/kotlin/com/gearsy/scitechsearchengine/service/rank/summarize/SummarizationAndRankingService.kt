package com.gearsy.scitechsearchengine.service.rank.summarize

import com.gearsy.scitechsearchengine.service.query.expansion.QueryExpansionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SummarizationAndRankingService(

) {
    private val logger = LoggerFactory.getLogger(SummarizationAndRankingService::class.java)

    suspend fun generateRankedResults(
        downloadPath: String,
        queryText: String,
        documentList: List<YandexSearchResultModel>
    ): List<SearchResult> {
        // 1. Генерация эмбеддинга запроса
        val queryEmbedding = embeddingService.generateEmbeddings(listOf(queryText)).first()

        // 2. Загрузка документов и генерация эмбеддингов
        val texts = documentList.map { doc ->
            val pdfPath = Paths.get(downloadPath, "${doc.documentId}.pdf").toFile()
            val content = extractTextFromPdf(pdfPath)
            cleanText(content)
        }

        val documentEmbeddings = embeddingService.generateEmbeddings(texts)

        // 3. Ранжирование по косинусному сходству
        val scoredResults = documentList.mapIndexed { index, doc ->
            val score = cosineSimilarity(queryEmbedding, documentEmbeddings[index])
            val snippet = generateSummary(texts[index])
            SearchResult(
                documentId = doc.documentId,
                documentUrl = doc.url,
                title = doc.title,
                snippet = snippet,
                score = score,
                query = Query(id = 119L) // временно подставим — заменить на реальный query
            )
        }

        // 4. Сортировка по score
        return scoredResults.sortedByDescending { it.score }
    }
}