package com.gearsy.scitechsearchengine.utils

import com.gearsy.scitechsearchengine.config.properties.spring.Neo4jProperties
import jakarta.annotation.PreDestroy
import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class Neo4jDriverProvider(
    neo4jProperties: Neo4jProperties
) {

    private val logger = LoggerFactory.getLogger(Neo4jDriverProvider::class.java)

    val driver: Driver = GraphDatabase.driver(
        neo4jProperties.uri,
        AuthTokens.basic(
            neo4jProperties.authentication.username,
            neo4jProperties.authentication.password
        )
    )

    @PreDestroy
    fun cleanup() {
        driver.close()
    }

    fun clearDatabase() {
        // TODO
        driver.session().use { session ->
            session.run("MATCH (n) DETACH DELETE n")
        }
        logger.info("База данных Neo4j очищена")
    }
}