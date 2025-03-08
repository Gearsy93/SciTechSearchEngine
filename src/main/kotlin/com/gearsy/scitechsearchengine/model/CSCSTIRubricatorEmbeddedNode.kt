package com.gearsy.scitechsearchengine.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonManagedReference

@JsonInclude(JsonInclude.Include.NON_NULL)
data class CSCSTIRubricatorEmbeddedNode(
    val cipher: String,
    val title: String,
    var termList: List<TermNode>? = null,
    val embedding: List<Float>,

    @JsonManagedReference
    var children: MutableList<CSCSTIRubricatorEmbeddedNode> = mutableListOf()
)
