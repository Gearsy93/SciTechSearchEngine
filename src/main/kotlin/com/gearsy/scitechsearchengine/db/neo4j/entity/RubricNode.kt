package com.gearsy.scitechsearchengine.db.neo4j.entity

import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node

@Node("Rubric")
data class RubricNode(
    @Id val thesaurusType: ThesaurusType,
    val sessionId: Long? = null,
    val queryId: Long? = null,
    val cipher: String,
    val title: String,
    val embedding: List<Double>,
    val termList: List<TermNode>? = null,
    val children: List<RubricNode> = emptyList()
)
