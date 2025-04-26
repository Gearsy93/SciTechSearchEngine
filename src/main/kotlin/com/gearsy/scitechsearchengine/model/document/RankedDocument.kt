package com.gearsy.scitechsearchengine.model.document

import com.gearsy.scitechsearchengine.model.yandex.SearchPrescription

data class RankedDocument(
    val documentId: String,
    val title: String,
    val url: String,
    val paragraph: ParagraphBlock,
    val snippet: String,
    val score: Double,
    val prescription: SearchPrescription
)
