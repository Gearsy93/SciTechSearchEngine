package com.gearsy.scitechsearchengine.db.postgres.entity

import jakarta.persistence.*

@Entity
@Table(name = "search_result", schema = "session")
data class SearchResult(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "query_id", nullable = false)
    val query: Query,

    @Column(name = "document_id", nullable = false)
    val documentId: String,  // новое поле

    @Column(name = "document_url", nullable = false)
    val documentUrl: String,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "snippet")
    val snippet: String? = null,

    @Column(name = "score")
    val score: Double? = null
)

