package com.gearsy.scitechsearchengine.model.engine

data class SelectedRubric(
    val cipher: String,
    val title: String,
    val relevantTerms: List<RelevantTerm>
)
