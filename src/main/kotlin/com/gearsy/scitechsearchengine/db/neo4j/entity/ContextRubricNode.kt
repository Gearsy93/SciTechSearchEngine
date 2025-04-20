package com.gearsy.scitechsearchengine.db.neo4j.entity

import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

@Node("ContextRubric")
data class ContextRubricNode(
    @Id val cipher: String,
    val title: String,

    @Relationship(type = "HAS_TERM", direction = Relationship.Direction.OUTGOING)
    val termList: List<ContextTermNode> = emptyList()
)
