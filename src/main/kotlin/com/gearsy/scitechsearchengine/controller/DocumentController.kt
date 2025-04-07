package com.gearsy.scitechsearchengine.controller

import com.gearsy.scitechsearchengine.controller.dto.document.ViewedDocumentDTO
import com.gearsy.scitechsearchengine.db.postgres.entity.ViewedDocument
import com.gearsy.scitechsearchengine.db.postgres.repository.QueryRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.ViewedDocumentRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/document")
class DocumentController(
    private val viewedDocumentRepository: ViewedDocumentRepository,
    private val queryRepository: QueryRepository
) {

    @PostMapping("/viewed")
    fun markDocumentAsViewed(@RequestBody request: ViewedDocumentDTO): ResponseEntity<Void> {
        val entity = ViewedDocument(
            query = queryRepository.findById(request.queryId).orElseThrow(),
            documentId = request.documentId
        )
        viewedDocumentRepository.save(entity)
        return ResponseEntity.ok().build()
    }
}