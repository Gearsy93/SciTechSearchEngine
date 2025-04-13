package com.gearsy.scitechsearchengine.utils

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class RequestLoggingFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val start = System.currentTimeMillis()
        try {
            filterChain.doFilter(request, response)
            val duration = System.currentTimeMillis() - start
            logger.info("${request.method} ${request.requestURI} - ${response.status} (${duration}ms)")
        } catch (ex: Exception) {
            val duration = System.currentTimeMillis() - start
            val status = if (response.status == 200) 500 else response.status
            logger.error("${request.method} ${request.requestURI} - ${status} (${duration}ms)", ex)
            throw ex
        }
    }
}


