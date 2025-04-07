package com.gearsy.scitechsearchengine.db.postgres.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "query", schema = "session")
data class Query(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    val session: Session,

    @Column(name = "query_text", nullable = false)
    val queryText: String,

    @Column(name = "iteration", nullable = false)
    val iteration: Int = 1,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now()
)
