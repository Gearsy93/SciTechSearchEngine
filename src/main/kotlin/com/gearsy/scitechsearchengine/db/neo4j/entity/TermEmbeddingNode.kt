package com.gearsy.scitechsearchengine.db.neo4j.entity

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TermEmbeddingNode(
    val content: String,
    val embedding: List<Double>
)