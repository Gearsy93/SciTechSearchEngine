package com.gearsy.scitechsearchengine.model.thesaurus

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.gearsy.scitechsearchengine.db.neo4j.entity.TermEmbeddingNode

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CSCSTIRubricatorEmbeddedNode(
    val cipher: String,
    val title: String,
    var termList: List<TermEmbeddingNode>? = null,
    val embedding: List<Double>?,

    @JsonManagedReference
    var children: MutableList<CSCSTIRubricatorEmbeddedNode> = mutableListOf()
)
