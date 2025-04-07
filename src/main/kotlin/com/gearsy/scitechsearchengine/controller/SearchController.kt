package com.gearsy.scitechsearchengine.controller

import com.gearsy.scitechsearchengine.controller.dto.search.SearchRequest
import com.gearsy.scitechsearchengine.controller.dto.search.SearchResultDTO
import com.gearsy.scitechsearchengine.db.postgres.entity.SearchResult
import com.gearsy.scitechsearchengine.service.search.SearchConveyorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/search")
class SearchController(
    private val searchService: SearchConveyorService
) {
    @PostMapping
    fun search(@RequestBody request: SearchRequest): ResponseEntity<List<SearchResultDTO>> {
        val results = searchService.handleSearch(request.sessionId, request.query)
        return ResponseEntity.ok(results)
    }
    @GetMapping("/history/{sessionId}")
    fun getHistory(@PathVariable sessionId: Long): ResponseEntity<List<SearchResult>> {
        val history = searchService.getHistoryBySessionId(sessionId)
        return ResponseEntity.ok(history)
    }
}
