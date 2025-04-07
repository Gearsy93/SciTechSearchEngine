package com.gearsy.scitechsearchengine.db.postgres.repository

import com.gearsy.scitechsearchengine.db.postgres.entity.Session
import org.springframework.data.jpa.repository.JpaRepository

interface SessionRepository : JpaRepository<Session, Long> {
    fun findSessionsById(id: Long): MutableList<Session>
}