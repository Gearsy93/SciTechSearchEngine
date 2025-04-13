package com.gearsy.scitechsearchengine.db.postgres.repository

import com.gearsy.scitechsearchengine.db.postgres.entity.SearchResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SearchResultRepository : JpaRepository<SearchResult, Long> {
    fun findAllByQueryId(queryId: Long): List<SearchResult>

    @Query(
        """
    SELECT sr
    FROM SearchResult sr
    JOIN ViewedDocument vd ON vd.document.id = sr.id
    WHERE sr.query.session.id = :sessionId
    """
    )
    fun findViewedBySessionId(@Param("sessionId") sessionId: Long): List<SearchResult>
    @Query("""
    SELECT s FROM SearchResult s
    WHERE s.documentId = :documentId AND s.query.session.id = :sessionId
""")
    fun findByDocumentIdAndSessionId(
        @Param("documentId") documentId: String,
        @Param("sessionId") sessionId: Long
    ): SearchResult?



}