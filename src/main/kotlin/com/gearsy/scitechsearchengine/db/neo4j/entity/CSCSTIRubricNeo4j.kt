package com.gearsy.scitechsearchengine.db.neo4j.entity

import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Relationship

@Node("Rubric")
data class CSCSTIRubricNeo4j(
    @Id
    val cipher: String,
    val title: String,
    val embedding: List<Double>?,
    @Relationship(type = "HAS_CHILD", direction = Relationship.Direction.OUTGOING)
    val children: List<CSCSTIRubricNeo4j>? = null,
    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    val termList: List<TermEmbeddingNode>? = null
)
