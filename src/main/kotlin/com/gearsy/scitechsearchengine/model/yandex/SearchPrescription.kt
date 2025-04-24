package com.gearsy.scitechsearchengine.model.yandex

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class SearchPrescription @JsonCreator constructor(
    @JsonProperty("queryText") val queryText: String,
    @JsonProperty("generatedText") val generatedText: String,
    @JsonProperty("terms") val terms: List<PrescriptionTerm>
)