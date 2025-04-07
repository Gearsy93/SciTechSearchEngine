package com.gearsy.scitechsearchengine.controller

import com.gearsy.scitechsearchengine.controller.dto.query.SearchResultResponseDTO
import com.gearsy.scitechsearchengine.db.postgres.entity.SearchResult
import com.gearsy.scitechsearchengine.db.postgres.repository.SearchResultRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/query")
class QueryController(
    private val searchResultRepository: SearchResultRepository
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

}