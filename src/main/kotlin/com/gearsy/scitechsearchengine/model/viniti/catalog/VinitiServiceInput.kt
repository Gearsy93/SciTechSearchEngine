package com.gearsy.scitechsearchengine.model.viniti.catalog

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VinitiServiceInput(
    val rubricCodes: List<String>,
    val maxPages: Int,
    val queryId: Long,
    val sessionId: Long
)