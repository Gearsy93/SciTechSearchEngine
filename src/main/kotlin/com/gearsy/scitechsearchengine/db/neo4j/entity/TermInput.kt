package com.gearsy.scitechsearchengine.db.neo4j.entity

data class TermInput(
    val content: String,
    val embedding: List<Double>,
    val thesaurusType: String,
    val sourceType: String,
    val sessionId: Long?,
    val queryId: Long?,
    val cipher: String
)
