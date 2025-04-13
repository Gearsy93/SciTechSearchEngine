package com.gearsy.scitechsearchengine.config.cli

import com.gearsy.scitechsearchengine.model.vinitiCatalog.VinitiServiceInput
import com.gearsy.scitechsearchengine.service.external.VinitiSearchService
import com.gearsy.scitechsearchengine.service.external.YandexService
import com.gearsy.scitechsearchengine.service.search.SearchConveyorService
import com.gearsy.scitechsearchengine.service.thesaurus.Neo4jFillerService
import com.gearsy.scitechsearchengine.service.thesaurus.RubricSearchAlgorithmService
import com.gearsy.scitechsearchengine.service.thesaurus.TerminologicalThesaurusService
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class ConsoleArgsRunner(
    private val neo4jDBFillerService: Neo4jFillerService,
    private val termThesaurusFormService: TerminologicalThesaurusService,
    private val relevantRubricTermSearchService: RubricSearchAlgorithmService,
    private val yandexAPIInteractionService: YandexService,
    private val vinitiDocSearchService: VinitiSearchService,
    private val searchConveyorService: SearchConveyorService
) : CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        if (args.isNotEmpty()) {
            val arguments = args.filterNotNull()

            if (arguments.isEmpty()) {
                printUsage()
                return
            }

            when {
                arguments.contains("-fillNeo4j") -> {
                    val index = arguments.indexOf("-fillNeo4j")
                    if (arguments.size > index + 1) {
                        val fileName = arguments[index + 1]
                        if (fileName.contains('.')) {
                            log.info("Ошибка: имя файла не должно содержать расширение.")
                            printUsage()
                        } else {
                            log.info("Выполняется: Заполнить схему Neo4j с файлом $fileName.json")
                            neo4jDBFillerService.fillNeo4jSchemaWithRubricJson(fileName)
                        }
                    } else {
                        log.info("Ошибка: флаг -fillNeo4j требует указания имени файла.")
                        printUsage()
                    }
                }

                arguments.contains("-clearNeo4j") -> {
                    log.info("Выполняется: Очистка базы данных Neo4j")
                    neo4jDBFillerService.clearDatabase()
                }

                arguments.contains("-generateCSCSTIThesaurusVectors") -> {
                    val index = arguments.indexOf("-generateCSCSTIThesaurusVectors")
                    if (arguments.size > index + 1) {
                        val cscstiCipher = arguments[index + 1]
                        log.info("Выполняется: Генерация векторов тезауруса для рубрики CSCSTI ($cscstiCipher)")
                        termThesaurusFormService.generateCSCSTIThesaurusVectors(cscstiCipher)
                    } else {
                        log.info("Ошибка: флаг -generateCSCSTIThesaurusVectors требует указания шифра рубрики.")
                        printUsage()
                    }
                }

                arguments.contains("-getQueryRelevantCSCSTIRubricList") -> {
                    val index = arguments.indexOf("-getQueryRelevantCSCSTIRubricList")
                    if (arguments.size > index + 1) {
                        val query = arguments[index + 1]
                        log.info("Выполняется: Поиск релевантных рубрик для запроса \"$query\"")
                        val results = relevantRubricTermSearchService.getQueryRelevantCSCSTIRubricTermList(query)

                        log.info("Релевантные рубрики:")
                        results.forEach { rubric ->
                            log.info("${rubric.cipher} - ${rubric.title}")
                            rubric.termList?.takeIf { it.isNotEmpty() }?.let { terms ->
                                log.info("  Релевантные термины: " + terms.joinToString(", ") { it.content })
                            }
                        }
                    } else {
                        log.info("Ошибка: флаг -getQueryRelevantCSCSTIRubricList требует указания запроса.")
                        printUsage()
                    }
                }

                arguments.contains("-make_search_api_request") -> {
                    log.info("Выполняется: Запрос Yandex Search API")
                    yandexAPIInteractionService.makeRequest()
                }

                arguments.contains("-make_e-catalog_request") -> {
                    log.info("Выполняется: Поиск по электронному каталогу ВИНИТИ")
                    val testInput = VinitiServiceInput(
                        rubricCodes = listOf("20.23.25", "27.17.25"),
                        maxPages = 5,
                        queryId = 1,
                        requestId = 1
                    )
                    vinitiDocSearchService.makeRequest(testInput)
                }

                arguments.contains("-run_search_conveyor") -> {
                    val index = arguments.indexOf("-run_search_conveyor")
                    if (arguments.size > index + 1) {
                        val query = arguments[index + 1]
                        log.info("Выполняется: запуск конвейера семантического поиска для запроса \"$query\"")
                        searchConveyorService.performSearchConveyor(query)
                    } else {
                        log.info("Ошибка: флаг -run_search_conveyor требует указания запроса.")
                        printUsage()
                    }
                }


                else -> {
                    log.info("Неизвестные аргументы.")
                    printUsage()
                }
            }
        }

    }

    private fun printUsage() {
        log.info(
            """
            Использование:
              -fillNeo4j <filename>                         Заполнить схему Neo4j с указанным именем файла (без расширения)
              -clearNeo4j                                   Очистить базу данных Neo4j
              -generateCSCSTIThesaurusVectors <cipher>      Генерировать векторы тезауруса для указанной рубрики CSCSTI
              -getQueryRelevantCSCSTIRubricList <query>     Найти релевантные рубрики для указанного запроса
              -make_search_api_request                      Запрос Yandex Search API
              -make_e-catalog_request                       Поиск по электронному каталогу ВИНИТИ
              -run_search_conveyor <query>                  Запустить полный конвейер семантического поиска для запроса
            """.trimIndent()
        )
    }
}
