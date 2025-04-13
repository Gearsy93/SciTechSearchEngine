package com.gearsy.scitechsearchengine.model.vinitiCatalog

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VinitiServiceInput(
    val rubricCodes: List<String>,
    val maxPages: Int,
    val queryId: Long,
    val requestId: Long
)