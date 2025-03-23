package com.gearsy.scitechsearchengine.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonManagedReference

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CSCSTIRubricatorEmbeddedNode(
    val cipher: String,
    val title: String,
    var termList: List<TermEmbeddingNode>? = null,
    val embedding: List<Double>?,

    @JsonManagedReference
    var children: MutableList<CSCSTIRubricatorEmbeddedNode> = mutableListOf()
)
