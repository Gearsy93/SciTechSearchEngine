package com.gearsy.scitechsearchengine.db.postgres.entity

import jakarta.persistence.*

@Entity
@Table(name = "yandex_document", schema = "session")
data class YandexDocument(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val queryId: Long,
    @Column(columnDefinition = "text")
    val documentId: String,
    @Column(columnDefinition = "text")
    val title: String,
    @Column(columnDefinition = "text")
    val link: String
)
