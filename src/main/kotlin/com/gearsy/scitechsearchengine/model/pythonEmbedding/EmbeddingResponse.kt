package com.gearsy.scitechsearchengine.model.pythonEmbedding

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingResponse(
    val embedding: List<Float>
)