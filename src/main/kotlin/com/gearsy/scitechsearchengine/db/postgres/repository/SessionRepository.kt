package com.gearsy.scitechsearchengine.db.postgres.repository

import com.gearsy.scitechsearchengine.controller.dto.session.SessionWithTitleDTO
import com.gearsy.scitechsearchengine.db.postgres.entity.Session
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SessionRepository : JpaRepository<Session, Long> {

    fun findSessionsById(id: Long): MutableList<Session>

    @Query("""
        SELECT new com.gearsy.scitechsearchengine.controller.dto.session.SessionWithTitleDTO(
            s.id, s.startTime, q.queryText
        )
        FROM Session s
        LEFT JOIN Query q ON q.session.id = s.id
        WHERE q.id = (
            SELECT MIN(q2.id)
            FROM Query q2
            WHERE q2.session.id = s.id
        )
        ORDER BY s.startTime DESC
    """)
    fun findAllSessionsWithTitles(): List<SessionWithTitleDTO>
}



