package com.gearsy.scitechsearchengine.utils

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
class Neo4jConfig {

    @Bean
    fun transactionManager(driver: org.neo4j.driver.Driver): Neo4jTransactionManager {
        return Neo4jTransactionManager(driver)
    }
}
