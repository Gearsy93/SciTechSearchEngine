package com.gearsy.scitechsearchengine.service.query

import com.gearsy.scitechsearchengine.controller.dto.query.CreateQueryRequest
import com.gearsy.scitechsearchengine.db.postgres.entity.Query
import com.gearsy.scitechsearchengine.db.postgres.repository.QueryRepository
import com.gearsy.scitechsearchengine.db.postgres.repository.SessionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QueryService(
    private val queryRepository: QueryRepository,
    private val sessionRepository: SessionRepository
) {

    fun getQueryById(id: Long): Query {
        return queryRepository.findById(id).orElseThrow { RuntimeException("Query not found") }
    }

    fun createQuery(request: CreateQueryRequest): Query {
        val session = sessionRepository.findById(request.sessionId)
            .orElseThrow { RuntimeException("Session not found") }

        val query = Query(
            session = session,
            queryText = request.queryText
        )

        return queryRepository.save(query)
    }
}