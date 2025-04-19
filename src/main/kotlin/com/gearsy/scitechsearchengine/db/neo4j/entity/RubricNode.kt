package com.gearsy.scitechsearchengine.db.neo4j.entity

import com.gearsy.scitechsearchengine.model.thesaurus.RubricImportDTO
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import java.util.*

@Node("Rubric")
data class RubricNode(
    @Id @GeneratedValue val id: UUID = UUID.randomUUID(),
    var thesaurusType: ThesaurusType?,
    val sessionId: Long? = null,
    val queryId: Long? = null,
    val cipher: String,
    val title: String,
    val embedding: List<Double>,
    val termList: List<TermNode>? = null,
    val children: List<RubricNode> = emptyList()

)