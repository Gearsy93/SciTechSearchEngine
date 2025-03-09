package com.gearsy.scitechsearchengine.model

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class YandexSearchResult(
    val documentId: String,
    val url: String,
)