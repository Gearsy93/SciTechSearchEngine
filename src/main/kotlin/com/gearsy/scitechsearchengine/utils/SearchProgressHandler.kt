package com.gearsy.scitechsearchengine.utils

import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler

@Component
class SearchProgressHandler : TextWebSocketHandler() {
    val sessions = mutableSetOf<WebSocketSession>()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions += session
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        sessions -= session
    }

    fun broadcast(message: String) {
        sessions.forEach {
            it.sendMessage(TextMessage(message))
        }
    }
}
