package com.gearsy.scitechsearchengine.model.conveyor

data class RawSearchResult(
    val documentId: String,
    val url: String,
    val title: String,
    val snippet: String?,
    val score: Double?
)
