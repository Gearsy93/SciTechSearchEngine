package com.gearsy.scitechsearchengine.config

import com.gearsy.scitechsearchengine.service.Neo4jDBFillerService
import com.gearsy.scitechsearchengine.service.TermThesaurusFormService
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class ConsoleArgsRunner(
    private val neo4jDBFillerService: Neo4jDBFillerService,
    private val termThesaurusFormService: TermThesaurusFormService
) : CommandLineRunner {

    override fun run(vararg args: String?) {
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
            else -> {
                println("Неизвестные аргументы.")
                printUsage()
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
            """.trimIndent()
        )
    }
}