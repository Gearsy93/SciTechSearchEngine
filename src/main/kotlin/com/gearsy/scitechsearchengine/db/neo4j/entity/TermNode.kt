package com.gearsy.scitechsearchengine.db.neo4j.entity

import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node

@Node("Term")
data class TermNode(
    @Id val content: String,
    val embedding: List<Double>,
    val weight: Double? = null,
    val type: ThesaurusType
)
