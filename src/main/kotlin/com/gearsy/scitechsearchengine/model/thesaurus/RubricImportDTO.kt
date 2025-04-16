package com.gearsy.scitechsearchengine.model.thesaurus

data class RubricImportDTO(
    val cipher: String,
    val title: String,
    val embedding: List<Double>,
    val termList: List<RubricTermImportDTO>? = null,
    val linkCipherList: List<String>? = null,
    val parentCipher: String? = null,
    val children: List<RubricImportDTO> = emptyList()
)
