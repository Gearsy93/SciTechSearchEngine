package com.gearsy.scitechsearchengine.config.cli

import com.gearsy.scitechsearchengine.config.properties.VinitiECatalogProperties
import com.gearsy.scitechsearchengine.controller.QueryController
import com.gearsy.scitechsearchengine.model.viniti.catalog.VinitiServiceInput
import com.gearsy.scitechsearchengine.service.external.VinitiSearchService
import com.gearsy.scitechsearchengine.service.external.YandexService
import com.gearsy.scitechsearchengine.service.rank.summarize.SummarizationAndRankingService
import com.gearsy.scitechsearchengine.service.search.SearchConveyorService
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricSearchAlgorithmService
import com.gearsy.scitechsearchengine.service.thesaurus.type.TerminologicalThesaurusService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import kotlin.math.abs
import kotlin.random.Random.Default.nextInt
import kotlin.random.Random.Default.nextLong

@Component
class ConsoleArgsRunner(
//    private val queryService: QueryService,
    private val queryController: QueryController,
    private val terminologicalThesaurusService: TerminologicalThesaurusService,
    private val rubricSearchAlgorithmService: RubricSearchAlgorithmService,
    private val yandexAPIInteractionService: YandexService,
    private val vinitiDocSearchService: VinitiSearchService,
    private val vinitiECatalogProperties: VinitiECatalogProperties,
    private val searchConveyorService: SearchConveyorService,
    private val summarizationAndRankingService: SummarizationAndRankingService
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        if (args.isNotEmpty()) {
            val arguments = args.filterNotNull()

            if (arguments.isEmpty()) {
                return
            }

            when {
                arguments.contains("-generate_term_thesaurus_embeddings") -> {
                    val testCipher = "20"
                    terminologicalThesaurusService.generateTermThesaurusEmbeddings(testCipher)
                }

                arguments.contains("-import_term_thesaurus") -> {
                    val testCipher = "65"
                    terminologicalThesaurusService.fillTermThesaurus(testCipher)
                }

                arguments.contains("-get_query_relevant_rubric_term_list") -> {
                    val query = "Математическая модель пищевого производства"
                    rubricSearchAlgorithmService.getRelevantTermListFromTermThesaurus(query)
                }

                arguments.contains("-make_e-catalog_request") -> {
                    val testInput = VinitiServiceInput(
                        rubricCodes = listOf("27.45"),
                        maxPages = vinitiECatalogProperties.maxPages.toInt(),
                        queryId = 1,
                        sessionId = 1
                    )
                    vinitiDocSearchService.getActualRubricListTerm(testInput)
                }

                arguments.contains("-make_yandex_search_api_request") -> {
                    val queryId = 72L
                    val query = "filetype:pdf Математическая модель пищевого производства"
                    yandexAPIInteractionService.processUnstructuredSearsh(query, queryId)
                }

                arguments.contains("-run_search_conveyor") -> {
                    val testQueryText = "Цифровая трансформация производства"
                    val sessionId = 72L
//                     searchConveyorService.performSearchConveyor(testQueryText, sessionId, testQueryText)
                }

                arguments.contains("-run_rank_summarize") -> {
                    summarizationAndRankingService.
                }

                else -> {
                    logger.info("Неизвестные аргументы.")
                }
            }
        }

    }

    fun extractTextFromPdf(file: File): String {
        PDDocument.load(file).use { document ->
            return PDFTextStripper().getText(document)
        }
    }

    fun cleanText(text: String): String {
        return text.replace(Regex("\\s+"), " ").trim()
    }

    fun cosineSimilarity(vec1: List<Double>, vec2: List<Double>): Double {
        val dot = vec1.zip(vec2).sumOf { it.first * it.second }
        val norm1 = kotlin.math.sqrt(vec1.sumOf { it * it }.toDouble())
        val norm2 = kotlin.math.sqrt(vec2.sumOf { it * it }.toDouble())
        return if (norm1 == 0.0 || norm2 == 0.0) 0.0 else dot / (norm1 * norm2)
    }

    fun generateSummary(text: String): String {
        // Простое реферирование — первый абзац
        return text.split(Regex("[.!?]")).take(2).joinToString(". ") + "..."
    }

}
