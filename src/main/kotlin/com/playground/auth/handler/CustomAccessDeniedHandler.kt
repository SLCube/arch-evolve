package com.playground.auth.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.common.error.ErrorCode
import com.playground.common.error.ErrorResponse
import com.playground.common.log.utils.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class CustomAccessDeniedHandler(
    private val objectMapper: ObjectMapper
): AccessDeniedHandler {

    private val log = logger()

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        log.warn("Access denied: {}", accessDeniedException.message)

        response.status = HttpServletResponse.SC_FORBIDDEN
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()

        val errorResponse = ErrorResponse(
            code = ErrorCode.FORBIDDEN.code,
            message = ErrorCode.FORBIDDEN.message(),
        )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}