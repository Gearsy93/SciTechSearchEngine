package com.gearsy.scitechsearchengine.model.document

data class ParagraphWithCandidates(
    val paragraph: ParagraphBlock,
    val bestSentence: SentenceCandidate?
)
