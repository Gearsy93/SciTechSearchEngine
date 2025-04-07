package com.gearsy.scitechsearchengine.controller.dto.search

data class SearchResultDTO(
    val id: Long,
    val queryId: Long,
    val documentId: String,
    val documentUrl: String,
    val title: String,
    val snippet: String?,
    val score: Double?
)

