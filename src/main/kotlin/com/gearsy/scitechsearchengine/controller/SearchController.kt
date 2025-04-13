package com.gearsy.scitechsearchengine.controller

import com.gearsy.scitechsearchengine.controller.dto.search.SearchRequestDTO
import com.gearsy.scitechsearchengine.controller.dto.search.SearchResultResponseDTO
import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.entity.SearchResult
import com.gearsy.scitechsearchengine.db.postgres.entity.Session
import com.gearsy.scitechsearchengine.db.postgres.repository.QueryRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.SearchResultRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.SessionRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.ViewedDocumentRepository
import com.gearsy.scitechsearchengine.service.search.SearchConveyorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/search")
class SearchController(
    private val queryRepository: QueryRepository,
    private val searchConveyorService: SearchConveyorService,
    private val viewedDocumentRepository: ViewedDocumentRepository,
    private val searchResultRepository: SearchResultRepository
) {

    @PostMapping
    fun search(@RequestBody request: SearchRequestDTO): ResponseEntity<Map<String, Any>> {
        val query = queryRepository.save(
            Query(
                session = Session(id = request.sessionId),
                queryText = request.query,
                createdAt = LocalDateTime.now()
            )
        )

        val fakeResults = searchConveyorService.generateMockResults(query)

        // Получаем уже сохранённые documentId для текущего запроса
        val existingDocIds = searchResultRepository.findAllByQueryId(query.id)
            .map { it.documentId }
            .toSet()

        // Фильтруем только новые documentId
        val filteredResults = fakeResults.filter { it.documentId !in existingDocIds }

        // Сохраняем
        val savedResults = searchResultRepository.saveAll(filteredResults)

        // Получаем просмотренные document.id в пределах всей сессии
        val viewedDocsInSession = viewedDocumentRepository
            .findAllByQuerySessionId(request.sessionId)
            .map { it.document.documentId }
            .toSet()

        val dtos = savedResults.map {
            SearchResultResponseDTO(
                id = it.id,
                documentUrl = it.documentUrl,
                title = it.title,
                snippet = it.snippet,
                score = it.score ?: 0.0,
                viewed = viewedDocsInSession.contains(it.documentId)
            )
        }

        return ResponseEntity.ok(
            mapOf(
                "queryId" to query.id,
                "results" to dtos
            )
        )
    }

}


