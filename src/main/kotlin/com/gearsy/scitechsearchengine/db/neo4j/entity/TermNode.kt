package com.gearsy.scitechsearchengine.db.neo4j.entity

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import java.util.*

@Node("Term")
data class TermNode(
    @Id @GeneratedValue val id: UUID = UUID.randomUUID(),
    val content: String,
    val embedding: List<Double>,
    val weight: Double? = null,
    var thesaurusType: ThesaurusType?,
    val sourceType: TermSourceType?,
    val sessionId: Long? = null,
    val queryId: Long? = null
)


