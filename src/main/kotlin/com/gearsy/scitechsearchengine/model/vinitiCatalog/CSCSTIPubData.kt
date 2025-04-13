package com.gearsy.scitechsearchengine.model.vinitiCatalog

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CSCSTIPubData(
    val rubricCipher: String,
    val keywords: List<String>? = null
)