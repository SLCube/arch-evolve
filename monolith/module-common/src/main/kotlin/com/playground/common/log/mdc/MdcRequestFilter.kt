package com.playground.common.log.mdc

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class MdcRequestFilter: OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        return uri.startsWith("/actuator") || uri.startsWith("/health")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startNs = System.nanoTime()

        val requestId = request.getHeader(HeaderKeys.X_REQUEST_ID)
            ?.takeIf { it.isNotBlank() }
            ?.trim()
            ?: UUID.randomUUID().toString()

        MDC.put(MdcKeys.REQUEST_ID, requestId)
        MDC.put(MdcKeys.METHOD, request.method)
        MDC.put(MdcKeys.PATH, request.requestURI)
        request.getHeader("X-User-Id")?.let { MDC.put(MdcKeys.USER_ID, it) }

        response.setHeader(HeaderKeys.X_REQUEST_ID, requestId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            val tookMs = (System.nanoTime() - startNs) / 1_000_000
            MDC.put(MdcKeys.STATUS, response.status.toString())
            MDC.put(MdcKeys.DURATION_MS, tookMs.toString())
            MDC.remove(MdcKeys.REQUEST_ID)
            MDC.remove(MdcKeys.METHOD)
            MDC.remove(MdcKeys.PATH)
            MDC.remove(MdcKeys.USER_ID)
            MDC.remove(MdcKeys.STATUS)
            MDC.remove(MdcKeys.DURATION_MS)
        }
    }
}