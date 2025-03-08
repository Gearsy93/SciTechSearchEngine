package com.gearsy.scitechsearchengine.service

import com.gearsy.scitechsearchengine.config.properties.VinitiECatalogProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class VinitiDocSearchService(
    vinitiECatalogProperties: VinitiECatalogProperties
) {

    private val logger = LoggerFactory.getLogger(VinitiDocSearchService::class.java)

    private val startPageUrl = vinitiECatalogProperties.startPageUrl

    fun makeRequest() {

        val testCSCTIRubricCipher = "27"


    }
}