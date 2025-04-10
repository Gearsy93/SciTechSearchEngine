package com.gearsy.scitechsearchengine.controller.dto.query

data class CreateQueryRequest(
    val sessionId: Long,
    val queryText: String
)