package com.gearsy.scitechsearchengine.config.cli

import com.gearsy.scitechsearchengine.config.properties.VinitiECatalogProperties
import com.gearsy.scitechsearchengine.model.viniti.catalog.VinitiServiceInput
import com.gearsy.scitechsearchengine.service.external.VinitiSearchService
import com.gearsy.scitechsearchengine.service.external.YandexService
import com.gearsy.scitechsearchengine.service.search.SearchConveyorService
import com.gearsy.scitechsearchengine.service.thesaurus.terminological.RubricImportFromFileService
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricSearchAlgorithmService
import com.gearsy.scitechsearchengine.service.thesaurus.type.TerminologicalThesaurusService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import kotlin.random.Random.Default.nextLong

@Component
class ConsoleArgsRunner(
    private val searchConveyorService: SearchConveyorService,
    private val terminologicalThesaurusService: TerminologicalThesaurusService,
    private val rubricSearchAlgorithmService: RubricSearchAlgorithmService,
    private val rubricImportFromFileService: RubricImportFromFileService,
    private val yandexAPIInteractionService: YandexService,
    private val vinitiDocSearchService: VinitiSearchService,
    private val vinitiECatalogProperties: VinitiECatalogProperties,
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
                    rubricImportFromFileService.fillTermThesaurus(testCipher)
                }

                arguments.contains("-get_query_relevant_rubric_term_list") -> {
                    val query = "Математическая модель пищевого производства"
                    rubricSearchAlgorithmService.getRelevantTermListFromTermThesaurus(query)
                }

                arguments.contains("-make_e-catalog_request") -> {
                    val testInput = VinitiServiceInput(
                        rubricCodes = listOf("20.23.25", "27.17.25"),
                        maxPages = vinitiECatalogProperties.maxPages.toInt(),
                        queryId = 1,
                        requestId = 1
                    )
                    vinitiDocSearchService.makeRequest(testInput)
                }

                arguments.contains("-make_yandex_search_api_request") -> {
                    yandexAPIInteractionService.makeRequest()
                }

                arguments.contains("-run_search_conveyor") -> {
                    val testQuery = "цифровая трансформация производства"
                    val testQueryId = nextLong()
                    searchConveyorService.performSearchConveyor(testQueryId, testQuery)
                }

                else -> {
                    logger.info("Неизвестные аргументы.")
                }
            }
        }

    }
}
