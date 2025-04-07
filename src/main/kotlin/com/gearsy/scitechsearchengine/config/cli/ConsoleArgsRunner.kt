package com.gearsy.scitechsearchengine.config.cli

import com.gearsy.scitechsearchengine.service.external.VinitiSearchService
import com.gearsy.scitechsearchengine.service.external.YandexService
import com.gearsy.scitechsearchengine.service.thesaurus.Neo4jFillerService
import com.gearsy.scitechsearchengine.service.thesaurus.RubricSearchService
import com.gearsy.scitechsearchengine.service.thesaurus.TermFormService
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class ConsoleArgsRunner(
    private val neo4jDBFillerService: Neo4jFillerService,
    private val termThesaurusFormService: TermFormService,
    private val relevantRubricTermSearchService: RubricSearchService,
    private val yandexAPIInteractionService: YandexService,
    private val vinitiDocSearchService: VinitiSearchService
) : CommandLineRunner {

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
                            println("Ошибка: имя файла не должно содержать расширение.")
                            printUsage()
                        } else {
                            println("Выполняется: Заполнить схему Neo4j с файлом $fileName.json")
                            neo4jDBFillerService.fillNeo4jSchemaWithRubricJson(fileName)
                        }
                    } else {
                        println("Ошибка: флаг -fillNeo4j требует указания имени файла.")
                        printUsage()
                    }
                }

                arguments.contains("-clearNeo4j") -> {
                    println("Выполняется: Очистка базы данных Neo4j")
                    neo4jDBFillerService.clearDatabase()
                }

                arguments.contains("-generateCSCSTIThesaurusVectors") -> {
                    val index = arguments.indexOf("-generateCSCSTIThesaurusVectors")
                    if (arguments.size > index + 1) {
                        val cscstiCipher = arguments[index + 1]
                        println("Выполняется: Генерация векторов тезауруса для рубрики CSCSTI ($cscstiCipher)")
                        termThesaurusFormService.generateCSCSTIThesaurusVectors(cscstiCipher)
                    } else {
                        println("Ошибка: флаг -generateCSCSTIThesaurusVectors требует указания шифра рубрики.")
                        printUsage()
                    }
                }

                arguments.contains("-getQueryRelevantCSCSTIRubricList") -> {
                    val index = arguments.indexOf("-getQueryRelevantCSCSTIRubricList")
                    if (arguments.size > index + 1) {
                        val query = arguments[index + 1]
                        println("Выполняется: Поиск релевантных рубрик для запроса \"$query\"")
                        val results = relevantRubricTermSearchService.getQueryRelevantCSCSTIRubricList(query)

                        println("Релевантные рубрики:")
                        results.forEach { rubric ->
                            println("${rubric.cipher} - ${rubric.title}")
                            rubric.termList?.takeIf { it.isNotEmpty() }?.let { terms ->
                                println("  Релевантные термины: " + terms.joinToString(", ") { it.content })
                            }
                        }
                    } else {
                        println("Ошибка: флаг -getQueryRelevantCSCSTIRubricList требует указания запроса.")
                        printUsage()
                    }
                }


                arguments.contains("-make_search_api_request") -> {
                    println("Выполняется: Запрос Yandex Search API")
                    yandexAPIInteractionService.makeRequest()
                }

                arguments.contains("-make_e-catalog_request") -> {
                    println("Выполняется: Поиск по электронному каталогу ВИНИТИ")
                    vinitiDocSearchService.makeRequest()
                }

                else -> {
                    println("Неизвестные аргументы.")
                    printUsage()
                }
            }
        }

    }

    private fun printUsage() {
        println(
            """
            Использование:
              -fillNeo4j <filename>                Заполнить схему Neo4j с указанным именем файла (без расширения)
              -clearNeo4j                          Очистить базу данных Neo4j
              -generateCSCSTIThesaurusVectors <cipher>  Генерировать векторы тезауруса для указанной рубрики CSCSTI
              -getQueryRelevantCSCSTIRubricList <query>  Найти релевантные рубрики для указанного запроса
              -make_search_api_request             Запрос Yandex Search API
              -make_e-catalog_request              Поиск по электронному каталогу ВИНИТИ
            """.trimIndent()
        )
    }
}
