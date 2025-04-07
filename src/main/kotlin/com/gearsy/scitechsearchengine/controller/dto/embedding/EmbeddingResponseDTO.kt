package com.gearsy.scitechsearchengine.controller.dto.embedding

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingResponseDTO(
    val embedding: List<Float>
)