package com.gearsy.scitechsearchengine.controller.dto.search

data class SearchRequestDTO(
    val sessionId: Long,
    val query: String
)