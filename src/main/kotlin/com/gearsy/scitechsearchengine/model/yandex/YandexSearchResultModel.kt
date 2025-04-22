package com.gearsy.scitechsearchengine.model.yandex

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class YandexSearchResultModel(
    val documentId: String,
    val title: String,
    val url: String,
)