package com.gearsy.scitechsearchengine.config.cli

import com.gearsy.scitechsearchengine.config.properties.VinitiECatalogProperties
import com.gearsy.scitechsearchengine.model.viniti.catalog.VinitiServiceInput
import com.gearsy.scitechsearchengine.service.external.VinitiSearchService
import com.gearsy.scitechsearchengine.service.external.YandexService
import com.gearsy.scitechsearchengine.service.search.SearchConveyorService
import com.gearsy.scitechsearchengine.service.thesaurus.type.TerminologicalThesaurusService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class ConsoleArgsRunner(
    private val searchConveyorService: SearchConveyorService,
    private val terminologicalThesaurusService: TerminologicalThesaurusService,
    private val yandexAPIInteractionService: YandexService,
    private val vinitiDocSearchService: VinitiSearchService,
    private val vinitiECatalogProperties: VinitiECatalogProperties,
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        if (args.isNotEmpty()) {
            val arguments = args.filterNotNull()

            if (arguments.isEmpty()) {
                printUsage()
                return
            }

            when {
                arguments.contains("-generate_term_thesaurus_vectors") -> {
                    val index = arguments.indexOf("-generate_term_thesaurus_vectors")
                    if (arguments.size > index + 1) {
                        val rubricatorCipher = arguments[index + 1]
                        logger.info("Выполняется: Генерация векторов терминологического тезауруса ($rubricatorCipher)")
                        terminologicalThesaurusService.generateTermThesaurusEmbeddings(rubricatorCipher)
                    } else {
                        logger.info("Ошибка: флаг -generate_term_thesaurus_vectors требует указания шифра рубрики.")
                        printUsage()
                    }
                }

                arguments.contains("-make_yandex_search_api_request") -> {
                    logger.info("Выполняется: Запрос Yandex Search API")
                    yandexAPIInteractionService.makeRequest()
                }

                arguments.contains("-make_e-catalog_request") -> {
                    logger.info("Выполняется: Поиск по электронному каталогу ВИНИТИ")
                    val testInput = VinitiServiceInput(
                        rubricCodes = listOf("20.23.25", "27.17.25"),
                        maxPages = vinitiECatalogProperties.maxPages.toInt(),
                        queryId = 1,
                        requestId = 1
                    )
                    vinitiDocSearchService.makeRequest(testInput)
                }

                arguments.contains("-run_search_conveyor") -> {
                    val index = arguments.indexOf("-run_search_conveyor")
                    if (arguments.size > index + 1) {
                        val query = arguments[index + 1]
                        logger.info("Выполняется: запуск конвейера семантического поиска для запроса \"$query\"")
                        searchConveyorService.performSearchConveyor(query)
                    } else {
                        logger.info("Ошибка: флаг -run_search_conveyor требует указания запроса.")
                        printUsage()
                    }
                }

                else -> {
                    logger.info("Неизвестные аргументы.")
                    printUsage()
                }
            }
        }

    }

    private fun printUsage() {
        logger.info(
            """
            Использование:
              -generate_term_thesaurus_vectors <cipher>     Генерировать векторы терминологического тезауруса
              -make_yandex_search_api_request               Запрос Yandex Search API
              -make_e-catalog_request                       Поиск по электронному каталогу ВИНИТИ
              -run_search_conveyor <query>                  Запустить полный конвейер семантического поиска для запроса
            """.trimIndent()
        )
    }
}
