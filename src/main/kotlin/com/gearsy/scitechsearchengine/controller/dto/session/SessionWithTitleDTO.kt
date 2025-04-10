package com.gearsy.scitechsearchengine.controller.dto.session

import java.time.LocalDateTime

data class SessionWithTitleDTO(
    val id: Long,
    val startTime: LocalDateTime,
    val title: String
)
