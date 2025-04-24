package com.gearsy.scitechsearchengine.model.yandex

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class YandexSearchResultModel @JsonCreator constructor(
    @JsonProperty("documentId") val documentId: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("url") val url: String,
    @JsonProperty("prescription") val prescription: SearchPrescription
)