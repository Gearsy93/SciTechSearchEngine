package com.gearsy.scitechsearchengine.service.thesaurus

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.gearsy.scitechsearchengine.config.properties.Neo4jProperties
import com.gearsy.scitechsearchengine.model.thesaurus.CSCSTIRubricatorEmbeddedNode
import jakarta.annotation.PreDestroy
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.neo4j.driver.Session
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

@Service
class Neo4jFillerService(
    neo4jProperties: Neo4jProperties,
) {

    private val logger = LoggerFactory.getLogger(Neo4jFillerService::class.java)

    // Создаем драйвер для подключения
    private var driver: Driver = GraphDatabase.driver(
        neo4jProperties.uri, AuthTokens.basic(neo4jProperties.authentication.username, neo4jProperties.authentication.password)
    )

    @PreDestroy
    fun cleanup() {
        driver.close()
    }

    fun fillNeo4jSchemaWithRubricJson(cscstiCipher: String) {

        // Путь к файлу JSON
        val filePath = "src/main/resources/rubricator/cscstiEmbeddings/$cscstiCipher.json"

        val mapper = jacksonObjectMapper()
        val rootRubric: CSCSTIRubricatorEmbeddedNode = mapper.readValue(File(filePath))

        // Открываем сессию и заполняем базу
        driver.session().use { session ->
            insertRubric(session, rootRubric, parentCipher = null)
        }
    }

    fun insertRubric(session: Session, rubricNode: CSCSTIRubricatorEmbeddedNode, parentCipher: String?) {
        logger.info("Создание рубрики: cipher=${rubricNode.cipher}, title=${rubricNode.title}")

        // Запрос для создания или обновления узла с меткой Rubric
        val query = """
                    MERGE (r:Rubric {cipher: ${'$'}cipher})
                    SET r.title = ${'$'}title, r.embedding = ${'$'}embedding
                    RETURN r
                    """.trimIndent()

        session.run(
            query, mapOf(
                "cipher" to rubricNode.cipher,
                "title" to rubricNode.title,
                "embedding" to rubricNode.embedding
            )
        )

        // Если существует родительская рубрика, создаём связь HAS_CHILD
        if (parentCipher != null) {
            val relQuery = """
                            MATCH (parent:Rubric {cipher: ${'$'}parentCipher}), (child:Rubric {cipher: ${'$'}childCipher})
                            MERGE (parent)-[:HAS_CHILD]->(child)
                           """.trimIndent()
            session.run(
                relQuery, mapOf(
                    "parentCipher" to parentCipher,
                    "childCipher" to rubricNode.cipher
                )
            )
        }

        // Обрабатываем термины, если они есть
        rubricNode.termList?.forEach { term ->

            val termQuery = """
                            MERGE (t:Term {content: ${'$'}content})
                            SET t.embedding = ${'$'}embedding
                            WITH t
                            MATCH (r:Rubric {cipher: ${'$'}cipher})
                            MERGE (t)-[:BELONGS_TO]->(r)
                            """.trimIndent()

            session.run(
                termQuery, mapOf(
                    "content" to term.content,
                    "embedding" to term.embedding,
                    "cipher" to rubricNode.cipher
                )
            )
        }

        // Рекурсивно обрабатываем дочерние рубрики
        rubricNode.children.forEach { child ->
            insertRubric(session, child, rubricNode.cipher)
        }
    }

    fun clearDatabase() {
        driver.session().use { session ->
            session.run("MATCH (n) DETACH DELETE n")
        }
        logger.info("База данных Neo4j очищена")
    }
}
