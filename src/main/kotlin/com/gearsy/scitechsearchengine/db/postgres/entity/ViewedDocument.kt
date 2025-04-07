package com.gearsy.scitechsearchengine.db.postgres.entity

import jakarta.persistence.*

@Entity
@Table(name = "viewed_document", schema = "session")
data class ViewedDocument(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "query_id", nullable = false)
    val query: Query,

    @Column(name = "document_id", nullable = false)
    val documentId: String
)
