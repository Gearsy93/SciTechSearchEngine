package com.gearsy.scitechsearchengine.model.document

import mikera.vectorz.Vector

data class SentenceCandidate(
    val sentence: String,
    val embedding: Vector,
    val similarityToQuery: Double,
    val noveltyPenalty: Double,
    val positionPenalty: Double,
    val score: Double
)