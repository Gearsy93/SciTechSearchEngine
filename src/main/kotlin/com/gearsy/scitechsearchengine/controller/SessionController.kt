package com.gearsy.scitechsearchengine.controller

import com.gearsy.scitechsearchengine.controller.dto.query.QueryListResponseDTO
import com.gearsy.scitechsearchengine.controller.dto.session.SessionResponseDTO
import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.entity.Session
import com.gearsy.scitechsearchengine.db.postgres.repository.QueryRepository
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
}
