package com.gearsy.scitechsearchengine.model.pythonEmbedding

import kotlinx.serialization.Serializable

@Serializable
data class EmbeddingRequest(
    val term: String,
    val context: List<String>,
    val title: String
)