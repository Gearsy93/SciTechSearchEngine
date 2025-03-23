package com.gearsy.scitechsearchengine.model.pythonEmbedding

import kotlinx.serialization.Serializable

@Serializable
data class RubricEmbeddingRequest(
    val title: String,
    val terms: List<String>
)