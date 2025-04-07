package com.gearsy.scitechsearchengine.service.search

import com.gearsy.scitechsearchengine.controller.dto.search.SearchResultDTO
import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.entity.SearchResult
import com.gearsy.scitechsearchengine.db.postgres.repository.QueryRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.SearchResultRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.SessionRepository
import com.gearsy.scitechsearchengine.model.engine.RawSearchResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
open class SearchConveyorService(
    private val sessionRepository: SessionRepository,
    private val queryRepository: QueryRepository,
    private val searchResultRepository: SearchResultRepository,
) {
    @Transactional
    fun handleSearch(sessionId: Long, userQueryText: String): List<SearchResultDTO> {
        val session = sessionRepository.findSessionsById(sessionId).first()
        val query = queryRepository.save(Query(session = session, queryText = userQueryText))

        val results = performSearch(userQueryText)

        val searchResults = results.map {
            searchResultRepository.save(
                SearchResult(
                    query = query,
                    documentId = it.documentId,  // передаем новый ID
                    documentUrl = it.url,
                    title = it.title,
                    snippet = it.snippet,
                    score = it.score
                )
            )
        }

        return searchResults.map {
            SearchResultDTO(
                id = it.id,
                queryId = query.id,
                documentId = it.documentId,
                documentUrl = it.documentUrl,
                title = it.title,
                snippet = it.snippet,
                score = it.score
            )
        }
    }



    fun performSearch(query: String): List<RawSearchResult> {
        return listOf(
            RawSearchResult("doc-1", "https://example.com", "Example Title", "Some snippet", 0.95),
            RawSearchResult("doc-2", "https://another.com", "Another Title", "Another snippet", 0.89)
        )
    }


    fun getHistoryBySessionId(sessionId: Long): List<SearchResult> {
        return searchResultRepository.findAllByQuerySessionId(sessionId)
    }

}