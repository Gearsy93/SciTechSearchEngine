package com.gearsy.scitechsearchengine.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "query-expansion")
class QueryExpansionProperties {
    lateinit var maxTermsPerQuery: String
    lateinit var maxCharsPerQuery: String
}