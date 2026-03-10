package com.playground.common.log.mdc

import com.playground.common.log.utils.logger
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class MdcRequestFilter : OncePerRequestFilter() {
    private val log = logger()

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

        MDC.put(MdcKeys.METHOD, request.method)
        MDC.put(MdcKeys.PATH, request.requestURI)
        request.getHeader("X-User-Id")?.let { MDC.put(MdcKeys.USER_ID, it) }

        try {
            filterChain.doFilter(request, response)
        } finally {
            val tookMs = (System.nanoTime() - startNs) / 1_000_000
            MDC.put(MdcKeys.STATUS, response.status.toString())
            MDC.put(MdcKeys.DURATION_MS, tookMs.toString())
            log.info("request completed")
            MDC.remove(MdcKeys.METHOD)
            MDC.remove(MdcKeys.PATH)
            MDC.remove(MdcKeys.USER_ID)
            MDC.remove(MdcKeys.STATUS)
            MDC.remove(MdcKeys.DURATION_MS)
        }
    }
}