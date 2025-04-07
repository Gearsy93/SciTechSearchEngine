package com.gearsy.scitechsearchengine.db.postgres.repository

import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import org.springframework.data.jpa.repository.JpaRepository

interface QueryRepository : JpaRepository<Query, Long> {
    fun findAllBySessionId(sessionId: Long): List<Query>
}