package com.gearsy.scitechsearchengine.db.postgres.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "session", schema = "session")
data class Session(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "start_time")
    val startTime: LocalDateTime = LocalDateTime.now()
)