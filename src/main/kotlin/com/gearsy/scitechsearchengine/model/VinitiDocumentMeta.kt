package com.gearsy.scitechsearchengine.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VinitiDocumentMeta(
    val title: String,
    val year: String,
    val annotation: String? = null,
    val cscstiKeywordGroups: MutableList<CSCSTIDocumentKeywords> = mutableListOf()
)