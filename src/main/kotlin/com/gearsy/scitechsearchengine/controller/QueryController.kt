package com.gearsy.scitechsearchengine.controller

import com.gearsy.scitechsearchengine.controller.dto.query.CreateQueryRequest
import com.gearsy.scitechsearchengine.controller.dto.query.CreateQueryResponse
import com.gearsy.scitechsearchengine.controller.dto.query.QueryDTO
import com.gearsy.scitechsearchengine.controller.dto.query.SearchResultResponseDTO
import com.gearsy.scitechsearchengine.db.postgres.repository.SearchResultRepository
import com.gearsy.scitechsearchengine.service.query.QueryService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/query")
class QueryController(
    private val searchResultRepository: SearchResultRepository,
    private val queryService: QueryService
) {

    @GetMapping("{queryId}/results")
    fun getSearchResultsByQuery(@PathVariable queryId: Long): ResponseEntity<List<SearchResultResponseDTO>> {
        val results = searchResultRepository.findAllByQueryId(queryId)
        val dtos = results.map {
            SearchResultResponseDTO(
                id = it.id,
                documentUrl = it.documentUrl,
                title = it.title,
                snippet = it.snippet,
                score = it.score
            )
        }
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/{id}")
    fun getQuery(@PathVariable id: Long): ResponseEntity<QueryDTO> {
        val query = queryService.getQueryById(id)
        return ResponseEntity.ok(QueryDTO(query.id, query.queryText))
    }

    @PostMapping("/create")
    fun createQuery(@RequestBody request: CreateQueryRequest): ResponseEntity<CreateQueryResponse> {
        val query = queryService.createQuery(request)
        return ResponseEntity.ok(CreateQueryResponse(query.id, query.session.id, query.queryText))
    }

}