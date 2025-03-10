package com.gearsy.scitechsearchengine.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "viniti.e-catalog")
class VinitiECatalogProperties {
    lateinit var startPageUrl: String
    lateinit var username: String
    lateinit var password: String
}