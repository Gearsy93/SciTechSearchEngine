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

    @ManyToOne
    @JoinColumn(name = "document_id", referencedColumnName = "id")
    val document: SearchResult


)
