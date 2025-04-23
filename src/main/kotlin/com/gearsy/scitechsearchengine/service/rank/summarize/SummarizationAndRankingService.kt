package com.gearsy.scitechsearchengine.service.rank.summarize

import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.entity.SearchResult
import com.gearsy.scitechsearchengine.model.document.ParagraphBlock
import com.gearsy.scitechsearchengine.model.yandex.YandexSearchResultModel
import com.gearsy.scitechsearchengine.service.lang.model.EmbeddingService
import kotlinx.coroutines.*
import mikera.vectorz.Vector
import org.jpedal.PdfDecoder
import org.jpedal.objects.PdfData

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class SummarizationAndRankingService(
    private val embeddingService: EmbeddingService
) {
    private val logger = LoggerFactory.getLogger(SummarizationAndRankingService::class.java)


    fun performRankingAndSummarization(
        query: Query,
        yandexResults: List<YandexSearchResultModel>
    ): List<SearchResult> {
        val downloadPath = "src/main/resources/session/yandexDocument/${query.id}"

        val allParagraphs: List<Pair<YandexSearchResultModel, List<ParagraphBlock>>> = runBlocking {
            coroutineScope {
                yandexResults.mapNotNull { doc ->
                    async(Dispatchers.IO) {
                        val file = File("$downloadPath/${doc.documentId}.pdf")
                        if (!file.exists()) {
                            logger.warn("Файл не найден: ${doc.documentId}")
                            return@async null
                        }

                        val paragraphBlocks = extractTextWithJPedalParallel(file.absolutePath)
                        logger.info("Извлечено ${paragraphBlocks.size} абзацев из ${doc.documentId}")
                        Pair(doc, paragraphBlocks)
                    }
                }.awaitAll().filterNotNull()
            }
        }

        val searchResultList = processDocumentsParagraphs(query, allParagraphs)

        return mutableListOf<SearchResult>().apply {}
    }

    fun extractTextWithJPedalParallel(path: String): List<ParagraphBlock> {
        val decoder = PdfDecoder()

        return try {
            decoder.setExtractionMode(PdfDecoder.TEXT)
            decoder.openPdfFile(path)
            val pageCount = decoder.pageCount

            val rawTextPerPage = MutableList(pageCount) { "" }

            runBlocking {
                coroutineScope {
                    (1..pageCount).map { page ->
                        async(Dispatchers.Default) {
                            val threadDecoder = PdfDecoder()
                            try {
                                threadDecoder.setExtractionMode(PdfDecoder.TEXT)
                                threadDecoder.openPdfFile(path)
                                threadDecoder.decodePage(page)
                                val data: PdfData = threadDecoder.pdfData
                                val count = data.getRawTextElementCount()

                                val rawText = buildString {
                                    for (i in 0 until count) {
                                        data.contents[i]?.let { append(it) }
                                    }
                                }

                                rawTextPerPage[page - 1] = rawText
                            } catch (e: Exception) {
                                rawTextPerPage[page - 1] = ""
                                e.printStackTrace()
                            } finally {
                                threadDecoder.closePdfFile()
                            }
                        }
                    }.awaitAll()
                }
            }

            val paragraphBlocks = mutableListOf<ParagraphBlock>()
            var prevTail: String? = null
            var paraIndex = 0

            for ((i, pageText) in rawTextPerPage.withIndex()) {
                val page = i + 1
                val cleaned = pageText
                    .replace(Regex("<[^>]+>"), "")
                    .replace(Regex("\\s+"), " ")
                    .replace(Regex("^\\[\\d{1,3}]\\s*"), "")
                    .trim()

                var paragraphs = cleaned.split(Regex("(?<=\\.)\\s+"))
                    .map { it.trim() }
                    .filter { it.length > 50 && it.count { ch -> ch == ' ' } > 5 }
                    .filterNot { para ->
                        val lower = para.lowercase()
                        listOf(
                            "удк", "ббк", "isbn", "формат", "министерство", "учебное издание",
                            "рис.", "xlabel", "ylabel", "plot", "таблица", "matlab"
                        ).any { it in lower } ||
                                para.matches(Regex("^\\d{1,3}$")) ||
                                para.matches(Regex("^\\p{Lu}{2,}.*$")) ||
                                para.contains("=") ||
                                para.contains("%") ||
                                para.matches(Regex(".*[a-zA-Z_]{2,}\\s*=.*")) ||
                                para.count { it == ',' } > 5 ||
                                para.matches(Regex(".*\\d+(\\.\\d+)?(\\s+\\d+(\\.\\d+)?){3,}.*"))
                    }


                if (prevTail != null && paragraphs.isNotEmpty()) {
                    val merged = prevTail + " " + paragraphs.first()
                    paragraphs = listOf(merged) + paragraphs.drop(1)
                }

                prevTail = paragraphs.lastOrNull()?.takeIf { !it.endsWith('.') }

                paragraphs.forEach { para ->
                    val cleanedPara = para.replace(Regex("^\\[\\d{1,3}]\\s*"), "")
                    paragraphBlocks.add(
                        ParagraphBlock(page = page, index = paraIndex++, text = cleanedPara)
                    )
                }
            }

            paragraphBlocks
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        } finally {
            decoder.closePdfFile()
        }
    }

    fun processDocumentsParagraphs(
        query: Query,
        documentsWithParagraphs: List<Pair<YandexSearchResultModel, List<ParagraphBlock>>>
    ): List<SearchResult> {
        // Здесь ты можешь:
        // - Разбить ParagraphBlock.text на предложения
        // - Генерировать эмбеддинги
        // - Рассчитать релевантность: новизна, позиция, покрытие
        // - Сформировать итоговый список SearchResult

        val queryEmbedding = Vector.of(*embeddingService.generateEmbeddings(listOf(query.queryText)).first().map(Float::toDouble).toDoubleArray())

        return documentsWithParagraphs.mapIndexed { index, (doc, paragraphs) ->
            SearchResult(
                query = query,
                documentId = doc.documentId,
                documentUrl = doc.url,
                title = doc.title,
                snippet = "", // заполни после ранжирования
                score = 0.0   // пока заглушка
            )
        }
    }

}