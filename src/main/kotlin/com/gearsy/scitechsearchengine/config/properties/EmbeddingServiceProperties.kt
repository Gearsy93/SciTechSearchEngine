package com.gearsy.scitechsearchengine.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "embedding-service")
class EmbeddingServiceProperties {
    lateinit var host: String
    lateinit var port: String
    lateinit var maxEmbeddingTermCount: String
    lateinit var maxSentenceTermCount: String
}