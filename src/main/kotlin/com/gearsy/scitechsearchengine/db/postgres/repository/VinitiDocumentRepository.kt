package com.gearsy.scitechsearchengine.db.postgres.repository

import com.gearsy.scitechsearchengine.db.postgres.entity.VinitiDocument
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VinitiDocumentRepository : JpaRepository<VinitiDocument, Long> {

    fun findAllByQueryId(queryId: Long): List<VinitiDocument>
}
