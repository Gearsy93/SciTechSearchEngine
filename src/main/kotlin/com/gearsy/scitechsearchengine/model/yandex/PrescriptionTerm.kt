package com.gearsy.scitechsearchengine.model.yandex

import com.gearsy.scitechsearchengine.db.neo4j.entity.TermSourceType
import com.gearsy.scitechsearchengine.db.neo4j.entity.ThesaurusType

data class PrescriptionTerm(
    val content: String,
    val weight: Double,
    val sourceType: TermSourceType,
    val thesaurusType: ThesaurusType,
    val embedding: List<Double>,
    val rubricCipher: String?,
    val rank: Int? = null
)