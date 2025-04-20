package com.gearsy.scitechsearchengine.db.postgres.entity

import jakarta.persistence.*


@Entity
@Table(name = "viniti_document_rubric_term", schema = "session")
data class VinitiDocumentRubricTerm(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "viniti_document_id")
    val document: VinitiDocument,

    @Column(name = "rubric_cipher")
    val rubricCipher: String,

    @Column(columnDefinition = "text")
    val keyword: String
)
