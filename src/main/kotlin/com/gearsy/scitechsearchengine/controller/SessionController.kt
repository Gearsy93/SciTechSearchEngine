package com.gearsy.scitechsearchengine.controller

import com.gearsy.scitechsearchengine.controller.dto.query.QueryListResponseDTO
import com.gearsy.scitechsearchengine.controller.dto.search.SearchResultResponseDTO
import com.gearsy.scitechsearchengine.controller.dto.session.SessionResponseDTO
import com.gearsy.scitechsearchengine.controller.dto.session.SessionWithTitleDTO
import com.gearsy.scitechsearchengine.db.postgres.entity.Session
import com.gearsy.scitechsearchengine.db.postgres.repository.QueryRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.SearchResultRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.SessionRepository
import com.gearsy.scitechsearchengine.service.session.SessionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/session")
class SessionController(
    private val sessionService: SessionService,
    private val sessionRepository: SessionRepository,
    private val searchResultRepository: SearchResultRepository,
    private val queryRepository: QueryRepository
) {
    @PostMapping("/create")
    fun createSession(): ResponseEntity<SessionResponseDTO> {
        val response = sessionService.createSession()
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/all")
    fun getAllSessions(): ResponseEntity<List<Session>> {
        return ResponseEntity.ok(sessionRepository.findAll())
    }

    @GetMapping("/{sessionId}/queries")
    fun getQueriesBySession(@PathVariable sessionId: Long): ResponseEntity<List<QueryListResponseDTO>> {
        val queries = queryRepository.findAllBySessionId(sessionId)
        val dtos = queries.map {
            QueryListResponseDTO(
                id = it.id,
                queryText = it.queryText,
                iteration = it.iteration,
                createdAt = it.createdAt
            )
        }
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/titled/all")
    fun getAllSessionsWithTitle(): ResponseEntity<List<SessionWithTitleDTO>> {
        val sessions = sessionRepository.findAllSessionsWithTitles()
        return ResponseEntity.ok(sessions)
    }

    @GetMapping("/{sessionId}/viewed")
    fun getViewedDocuments(@PathVariable sessionId: Long): ResponseEntity<List<SearchResultResponseDTO>> {
        val results = searchResultRepository.findViewedBySessionId(sessionId)

        val dtos = results.map {
            SearchResultResponseDTO(
                id = it.id,
                documentUrl = it.documentUrl,
                title = it.title,
                snippet = it.snippet,
                score = it.score!!,
                viewed = true
            )
        }

        return ResponseEntity.ok(dtos)
    }

}
