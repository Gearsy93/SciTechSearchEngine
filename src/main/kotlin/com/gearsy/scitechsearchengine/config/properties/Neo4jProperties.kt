package com.gearsy.scitechsearchengine.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "spring.neo4j")
class Neo4jProperties {
    lateinit var uri: String
    lateinit var authentication: Authentication
    var useStrictNodeIds: Boolean = false

    class Authentication {
        lateinit var username: String
        lateinit var password: String
    }
}
