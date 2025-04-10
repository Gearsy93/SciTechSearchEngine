package com.gearsy.scitechsearchengine.controller.dto.query

data class CreateQueryResponse(
    val id: Long,
    val sessionId: Long,
    val queryText: String
)
