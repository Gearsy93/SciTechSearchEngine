package com.gearsy.scitechsearchengine.controller

import com.gearsy.scitechsearchengine.controller.dto.search.SearchRequestDTO
import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.entity.Session
import com.gearsy.scitechsearchengine.db.postgres.repository.QueryRepository
import com.gearsy.scitechsearchengine.service.search.SearchConveyorService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/search")
class SearchController(
    private val queryRepository: QueryRepository,
    private val searchConveyorService: SearchConveyorService
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

        val dtos = searchConveyorService.handleSearchConveyor(request, query)

        return ResponseEntity.ok(
            mapOf(
                "queryId" to query.id,
                "results" to dtos
            )
        )
    }

}


