package com.gearsy.scitechsearchengine.controller

import com.gearsy.scitechsearchengine.controller.dto.document.ViewedDocumentDTO
import com.gearsy.scitechsearchengine.controller.dto.query.CreateQueryRequest
import com.gearsy.scitechsearchengine.controller.dto.query.CreateQueryResponse
import com.gearsy.scitechsearchengine.controller.dto.query.QueryDTO
import com.gearsy.scitechsearchengine.controller.dto.search.SearchResultResponseDTO
import com.gearsy.scitechsearchengine.db.postgres.repository.QueryRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.SearchResultRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.ViewedDocumentRepository
import com.gearsy.scitechsearchengine.service.query.QueryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/query")
class QueryController(
    private val searchResultRepository: SearchResultRepository,
    private val viewedDocumentRepository: ViewedDocumentRepository,
    private val queryRepository: QueryRepository,
    private val queryService: QueryService
) {

    @GetMapping("{queryId}/results")
    fun getSearchResultsByQuery(
        @PathVariable queryId: Long
    ): ResponseEntity<List<SearchResultResponseDTO>> {
        val query = queryRepository.findById(queryId).orElseThrow()
        val sessionId = query.session.id

        val viewedDocsInSession = viewedDocumentRepository.findAllBySessionId(sessionId)
            .map { it.document.documentId }
            .toSet()

        val results = searchResultRepository.findAllByQueryId(queryId)

        val dtos = results.map {
            val viewed = viewedDocsInSession.contains(it.documentId)
            SearchResultResponseDTO(
                id = it.id,
                documentUrl = it.documentUrl,
                title = it.title,
                snippet = it.snippet,
                score = it.score ?: 0.0,
                viewed = viewed
            )
        }

        return ResponseEntity.ok(dtos)
    }


    @GetMapping("/{id}")
    fun getQuery(@PathVariable id: Long): ResponseEntity<QueryDTO> {
        val query = queryService.getQueryById(id)
        return ResponseEntity.ok(QueryDTO(query.id, query.queryText))
    }

    @PostMapping("/new")
    fun createQuery(@RequestBody request: CreateQueryRequest): ResponseEntity<CreateQueryResponse> {
        val query = queryService.createQuery(request)
        return ResponseEntity.ok(CreateQueryResponse(query.id, query.session.id, query.queryText))
    }
}