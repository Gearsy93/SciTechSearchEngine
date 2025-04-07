package com.gearsy.scitechsearchengine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories

@SpringBootApplication
@EnableJpaRepositories(basePackages = ["com.gearsy.scitechsearchengine.db.postgres.repository"])
@EnableNeo4jRepositories(basePackages = ["com.gearsy.scitechsearchengine.db.neo4j.repository"])

class ScienceTechnologySearchEngineApplication

fun main(args: Array<String>) {
	runApplication<ScienceTechnologySearchEngineApplication>(*args)
}
