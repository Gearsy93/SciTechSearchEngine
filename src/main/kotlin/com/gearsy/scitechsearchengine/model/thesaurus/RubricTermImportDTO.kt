package com.gearsy.scitechsearchengine.model.thesaurus

data class RubricTermImportDTO(
    val content: String,
    val embedding: List<Double>,
    val rubricatorId: Int? = null
)
