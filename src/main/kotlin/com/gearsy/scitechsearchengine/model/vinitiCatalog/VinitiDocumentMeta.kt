package com.gearsy.scitechsearchengine.model.vinitiCatalog

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class VinitiDocumentMeta(
    val title: String,
    val annotation: String?,
    val translateTitle: String?,
    val link: String,
    val language: String,
    val pubDataList: List<CSCSTIPubData>
)