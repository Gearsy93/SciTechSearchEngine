package com.gearsy.scitechsearchengine.controller.dto.query

import java.time.LocalDateTime

data class QueryListResponseDTO(
    val id: Long,
    val queryText: String,
    val iteration: Int,
    val createdAt: LocalDateTime
)