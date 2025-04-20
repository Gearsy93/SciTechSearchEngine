package com.gearsy.scitechsearchengine.model.yandex

data class SearchPrescription(
    val queryText: String,
    val generatedText: String,
    val terms: List<PrescriptionTerm>
)