package com.gearsy.scitechsearchengine.model.engine

data class RawSearchResult(
    val documentId: String,
    val url: String,
    val title: String,
    val snippet: String?,
    val score: Double?
)
