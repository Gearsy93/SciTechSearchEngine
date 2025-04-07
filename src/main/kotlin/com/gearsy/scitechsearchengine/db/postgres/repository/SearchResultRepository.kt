package com.gearsy.scitechsearchengine.db.postgres.repository

import com.gearsy.scitechsearchengine.db.postgres.entity.SearchResult
import org.springframework.data.jpa.repository.JpaRepository

interface SearchResultRepository : JpaRepository<SearchResult, Long> {
    fun findAllByQueryId(queryId: Long): List<SearchResult>
    fun findAllByQuerySessionId(sessionId: Long): List<SearchResult>
}