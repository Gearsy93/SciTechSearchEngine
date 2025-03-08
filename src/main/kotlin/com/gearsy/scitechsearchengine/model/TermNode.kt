package com.gearsy.scitechsearchengine.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class TermNode(
    val content: String,
    val embedding: List<Float>
)