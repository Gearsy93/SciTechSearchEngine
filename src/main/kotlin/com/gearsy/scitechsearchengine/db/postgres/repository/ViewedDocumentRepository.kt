package com.gearsy.scitechsearchengine.db.postgres.repository

import com.gearsy.scitechsearchengine.db.postgres.entity.ViewedDocument
import org.springframework.data.jpa.repository.JpaRepository

interface ViewedDocumentRepository : JpaRepository<ViewedDocument, Long> {
    fun findAllByQueryId(queryId: Long): List<ViewedDocument>
}