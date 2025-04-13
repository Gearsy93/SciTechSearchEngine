package com.gearsy.scitechsearchengine.controller.dto.search

data class SearchResultResponseDTO(
    val id: Long,
    val documentUrl: String,
    val title: String,
    val snippet: String?,
    val score: Double,
    val viewed: Boolean
)