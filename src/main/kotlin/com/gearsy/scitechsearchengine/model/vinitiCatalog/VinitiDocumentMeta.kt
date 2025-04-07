package com.gearsy.scitechsearchengine.model.vinitiCatalog

import com.fasterxml.jackson.annotation.JsonInclude
import com.gearsy.scitechsearchengine.model.thesaurus.CSCSTIDocumentKeywords

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VinitiDocumentMeta(
    val title: String,
    val year: String,
    val annotation: String? = null,
    val cscstiKeywordGroups: MutableList<CSCSTIDocumentKeywords> = mutableListOf()
)