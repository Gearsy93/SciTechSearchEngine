package com.gearsy.scitechsearchengine.controller

import com.gearsy.scitechsearchengine.controller.dto.document.ViewedDocumentDTO
import com.gearsy.scitechsearchengine.db.postgres.entity.ViewedDocument
import com.gearsy.scitechsearchengine.db.postgres.repository.QueryRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.SearchResultRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.ViewedDocumentRepository
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/document")
class DocumentController(
    private val viewedDocumentRepository: ViewedDocumentRepository,
    private val queryRepository: QueryRepository,
    private val searchResultRepository: SearchResultRepository
) {

    @PostMapping("/viewed")
    fun markDocumentAsViewed(@RequestBody request: ViewedDocumentDTO): ResponseEntity<Void> {
        val query = queryRepository.findById(request.queryId).orElseThrow()
        val document = searchResultRepository.findById(request.documentId).orElseThrow()

        val entity = ViewedDocument(
            query = query,
            document = document
        )

        viewedDocumentRepository.save(entity)
        return ResponseEntity.ok().build()
    }


    @PostMapping("/{queryId}/unviewed")
    @Transactional
    fun unmarkDocumentAsViewed(
        @PathVariable queryId: Long,
        @RequestBody request: ViewedDocumentDTO
    ): ResponseEntity<Void> {
        val query = queryRepository.findById(queryId).orElseThrow()
        val document = searchResultRepository.findById(request.documentId).orElseThrow()

        val documentUuid = document.documentId // ← String

        val allViewed = viewedDocumentRepository.findAllByQuerySessionId(query.session.id)
        val docsToRemove = allViewed.filter { it.document.documentId == documentUuid }

        viewedDocumentRepository.deleteAll(docsToRemove)

        return ResponseEntity.ok().build()
    }



}