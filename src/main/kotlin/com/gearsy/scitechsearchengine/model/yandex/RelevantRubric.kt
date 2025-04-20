package com.gearsy.scitechsearchengine.model.yandex

data class RelevantRubric(
    val cipher: String,
    val title: String,
    val relevantTerms: List<RelevantTerm>
)
