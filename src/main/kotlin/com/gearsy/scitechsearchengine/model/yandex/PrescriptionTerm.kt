package com.gearsy.scitechsearchengine.model.yandex

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.gearsy.scitechsearchengine.db.neo4j.entity.TermSourceType
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType

data class PrescriptionTerm @JsonCreator constructor(
    @JsonProperty("content") val content: String,
    @JsonProperty("weight") val weight: Double,
    @JsonProperty("sourceType") val sourceType: TermSourceType,
    @JsonProperty("thesaurusType") val thesaurusType: ThesaurusType,
    @JsonProperty("embedding") val embedding: List<Double>,
    @JsonProperty("rubricCipher") val rubricCipher: String?,
    @JsonProperty("rank") val rank: Int? = null
)