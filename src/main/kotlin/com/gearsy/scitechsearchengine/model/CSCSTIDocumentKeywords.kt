package com.gearsy.scitechsearchengine.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CSCSTIDocumentKeywords(
    val cipher: String,
    val keywordList: MutableList<String> = mutableListOf()
)