package com.gearsy.scitechsearchengine.service.session

import com.gearsy.scitechsearchengine.controller.dto.session.SessionResponseDTO
import com.gearsy.scitechsearchengine.db.postgres.entity.Session
import com.gearsy.scitechsearchengine.db.postgres.repository.SessionRepository
import org.springframework.stereotype.Service

@Service
class SessionService(
    private val sessionRepository: SessionRepository
) {
    fun createSession(): SessionResponseDTO {
        val session = Session()
        val saved = sessionRepository.save(session)
        return SessionResponseDTO(
            sessionId = saved.id,
            startTime = saved.startTime.toString()
        )
    }
}
