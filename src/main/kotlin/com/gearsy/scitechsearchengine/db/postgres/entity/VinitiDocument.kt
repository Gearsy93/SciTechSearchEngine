package com.gearsy.scitechsearchengine.db.postgres.entity

import jakarta.persistence.*

@Entity
@Table(name = "viniti_document", schema = "session")
data class VinitiDocument(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "query_id")
    val query: Query,

    @Column(columnDefinition = "text")
    val title: String,
    @Column(columnDefinition = "text")
    val annotation: String? = null,
    @Column(name = "translate_title", columnDefinition = "text")
    val translateTitle: String? = null,
    @Column(columnDefinition = "text")
    val link: String,
    @Column(columnDefinition = "text")
    val language: String? = null,

    @OneToMany(mappedBy = "document", cascade = [CascadeType.ALL], orphanRemoval = true)
    val rubricTerms: MutableList<VinitiDocumentRubricTerm> = mutableListOf()
)
