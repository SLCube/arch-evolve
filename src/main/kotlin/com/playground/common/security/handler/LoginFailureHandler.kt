package com.playground.common.security.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.common.error.ErrorCode
import com.playground.common.error.ErrorResponse
import com.playground.common.utils.logger
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class LoginFailureHandler(
    private val objectMapper: ObjectMapper
): AuthenticationFailureHandler {

    private val log = logger()

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        log.warn("Authentication failed: {}", exception.message)

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()

        val errorResponse = ErrorResponse(
            code = ErrorCode.UNAUTHORIZED.code,
            message = ErrorCode.UNAUTHORIZED.message(),
        )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}