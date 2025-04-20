package com.gearsy.scitechsearchengine.config.cli

import com.gearsy.scitechsearchengine.config.properties.VinitiECatalogProperties
import com.gearsy.scitechsearchengine.controller.QueryController
import com.gearsy.scitechsearchengine.controller.dto.query.CreateQueryRequest
import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.entity.Session
import com.gearsy.scitechsearchengine.db.postgres.repository.QueryRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.SessionRepository
import com.gearsy.scitechsearchengine.model.viniti.catalog.VinitiServiceInput
import com.gearsy.scitechsearchengine.service.external.VinitiSearchService
import com.gearsy.scitechsearchengine.service.external.YandexService
import com.gearsy.scitechsearchengine.service.query.QueryService
import com.gearsy.scitechsearchengine.service.search.SearchConveyorService
import com.gearsy.scitechsearchengine.service.thesaurus.shared.RubricSearchAlgorithmService
import com.gearsy.scitechsearchengine.service.thesaurus.type.TerminologicalThesaurusService
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime

@Component
class ConsoleArgsRunner(
//    private val queryService: QueryService,
    private val queryController: QueryController,
    private val terminologicalThesaurusService: TerminologicalThesaurusService,
    private val rubricSearchAlgorithmService: RubricSearchAlgorithmService,
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
                    yandexAPIInteractionService.makeRequest()
                }

                arguments.contains("-run_search_conveyor") -> {
                    val testQueryText = "Применение графов в логистике и транспортных сетях"
                    val sessionId = 72L
                    // searchConveyorService.performSearchConveyor(testQuery, sessionId, testQueryText)
                }

                else -> {
                    logger.info("Неизвестные аргументы.")
                }
            }
        }

    }
}
