package com.gearsy.scitechsearchengine.db.neo4j.entity

import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import java.util.*

@Node("ContextTerm")
data class ContextTermNode(
    @Id @GeneratedValue val id: UUID = UUID.randomUUID(),
    val content: String,
    val count: Int = 1,
    val sessionId: Long,
    val lastQueryId: Long
)


