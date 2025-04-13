package com.gearsy.scitechsearchengine.db.postgres.repository

import com.gearsy.scitechsearchengine.db.postgres.entity.ViewedDocument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ViewedDocumentRepository : JpaRepository<ViewedDocument, Long> {
    fun findAllByQuerySessionId(sessionId: Long): List<ViewedDocument>

    @Query("""
    SELECT vd FROM ViewedDocument vd
    WHERE vd.query.session.id = :sessionId
""")
    fun findAllBySessionId(@Param("sessionId") sessionId: Long): List<ViewedDocument>

}