package com.gearsy.scitechsearchengine.config.cli

import com.gearsy.scitechsearchengine.config.properties.VinitiECatalogProperties
import com.gearsy.scitechsearchengine.controller.QueryController
import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.entity.Session
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

//                arguments.contains("-make_yandex_search_api_request") -> {
//                    val queryId = 119L
//                    val query = "filetype:pdf Математическая модель пищевого производства"
//                    yandexAPIInteractionService.getPrescriptionResultList(query, queryId)
//                }

                arguments.contains("-run_search_conveyor") -> {
                    val testQueryText = "Цифровая трансформация производства"
                    val sessionId = 72L
//                     searchConveyorService.performSearchConveyor(testQueryText, sessionId, testQueryText)
                }

                arguments.contains("-run_rank_summarize") -> {
                    val query = Query(119L, Session(), "Цифровая трансформация производства")
//                    val yandexResultList = getYandexResultsMock()
//                    summarizationAndRankingService.performRankingAndSummarization(query, yandexResultList)
                }

                else -> {
                    logger.info("Неизвестные аргументы.")
                }
            }
        }
    }
}
