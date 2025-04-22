package com.gearsy.scitechsearchengine.db.postgres.repository

import com.gearsy.scitechsearchengine.db.postgres.entity.YandexDocument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface YandexDocumentRepository : JpaRepository<YandexDocument, Long> {
    fun findAllByQueryId(queryId: Long): List<YandexDocument>
}
