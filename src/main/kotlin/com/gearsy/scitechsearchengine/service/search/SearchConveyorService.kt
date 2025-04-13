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
import java.util.*

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

    fun generateMockResults(query: Query): List<SearchResult> {
        val count = (5..15).random()
        val titles = listOf(
            "Искусственный интеллект в промышленности",
            "Цифровая трансформация производств",
            "Интернет вещей и автоматизация",
            "Умные фабрики и системы управления",
            "Оптимизация процессов с помощью ИИ",
            "Цифровизация производственных цепочек",
            "Автоматизация технологических процессов"
        )
        val used = mutableSetOf<String>()

        return (1..count).map { i ->
            val availableTitles = titles.filter { it !in used }
            val title = if (availableTitles.isNotEmpty()) {
                availableTitles.random()
            } else {
                "Автоматизированный заголовок №$i" // или просто titles.random(), если дубли допустимы
            }

            used.add(title)

            SearchResult(
                query = query,
                documentId = UUID.randomUUID().toString(),
                documentUrl = "https://example.com/doc-$i",
                title = title,
                snippet = "$title, $title, $title, $title, $title",
                score = "%.2f".format((0.6 + Math.random() * 0.39)).toDouble()
            )
        }
    }


}