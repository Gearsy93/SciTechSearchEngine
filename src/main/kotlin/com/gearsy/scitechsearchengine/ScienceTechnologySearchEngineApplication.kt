package com.gearsy.scitechsearchengine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableTransactionManagement
@EntityScan("com.gearsy.scitechsearchengine.db.postgres.entity")
@EnableJpaRepositories(basePackages = ["com.gearsy.scitechsearchengine.db.postgres.repository"])
@EnableNeo4jRepositories(basePackages = ["com.gearsy.scitechsearchengine.db.neo4j.repository"])

class ScienceTechnologySearchEngineApplication

fun main(args: Array<String>) {
	runApplication<ScienceTechnologySearchEngineApplication>(*args)
}
