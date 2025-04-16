package com.gearsy.scitechsearchengine.db.neo4j.entity

data class RubricInput(
    val cipher: String,
    val title: String,
    val parentCipher: String?,
    val embedding: List<Double>,
    val thesaurusType: String,
    val sessionId: Long?,
    val queryId: Long?
)
