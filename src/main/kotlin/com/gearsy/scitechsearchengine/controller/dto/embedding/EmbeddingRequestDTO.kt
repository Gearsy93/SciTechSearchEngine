package com.gearsy.scitechsearchengine.controller.dto.embedding

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingRequestDTO(
    val term: String,
    val context: List<String>,
    val title: String
)