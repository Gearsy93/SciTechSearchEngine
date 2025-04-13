package com.gearsy.scitechsearchengine.model.conveyor

data class SelectedRubric(
    val cipher: String,
    val title: String,
    val relevantTerms: List<RelevantTerm>
)
