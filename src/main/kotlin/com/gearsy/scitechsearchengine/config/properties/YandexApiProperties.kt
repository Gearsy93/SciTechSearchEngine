package com.gearsy.scitechsearchengine.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "external-api.yandex-cloud")
class YandexApiProperties {
    lateinit var apiKey: String
    lateinit var searchApiUrl: String
    lateinit var resultApiUrl: String
}