package com.gearsy.scitechsearchengine.model.viniti.catalog

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RubricTermData(
    val rubricCipher: String,
    val keywords: List<String>? = null
)