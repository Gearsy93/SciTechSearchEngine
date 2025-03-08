package com.gearsy.scitechsearchengine.service

import com.gearsy.scitechsearchengine.config.properties.YandexApiProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class YandexAPIInteractionService(
    yandexApiProperties: YandexApiProperties,
    private val restTemplate: RestTemplate,
) {
    private val logger = LoggerFactory.getLogger(YandexAPIInteractionService::class.java)

    private val apiKey = yandexApiProperties.apiKey
    private val searchApiUrl = yandexApiProperties.searchApiUrl


    fun makeRequest() {
        val query = "научно-техническая информация"
        val mimeTypes = listOf("doc", "docx", "pdf").joinToString("|") { "mime:$it" }

        val url = UriComponentsBuilder.fromHttpUrl(searchApiUrl)
            .queryParam("query", "$query $mimeTypes")
            .queryParam("user", "your_username")
            .queryParam("key", apiKey)
            .queryParam("page", 1)
            .queryParam("limit", 10)
            .toUriString()

        val headers = HttpHeaders()
        headers.set("Authorization", "Api-Key $apiKey")

        val requestEntity = HttpEntity<Void>(headers)

        try {
            val response: ResponseEntity<String> = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, String::class.java
            )

            logger.info("Yandex API Response: ${response.body}")
        } catch (ex: Exception) {
            logger.error("Error while calling Yandex API", ex)
        }
    }

}