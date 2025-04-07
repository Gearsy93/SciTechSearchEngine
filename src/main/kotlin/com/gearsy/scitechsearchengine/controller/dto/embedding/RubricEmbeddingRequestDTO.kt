package com.gearsy.scitechsearchengine.controller.dto.embedding

import kotlinx.serialization.Serializable

@Serializable
data class RubricEmbeddingRequestDTO(
    val title: String,
    val terms: List<String>
)