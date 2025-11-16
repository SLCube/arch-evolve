package com.playground.auth.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.auth.jwt.JwtTokenProvider
import com.playground.auth.presentation.response.AuthTokenResponseDto
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

@Component
class LoginSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider,
    private val objectMapper: ObjectMapper,
) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val accessToken = jwtTokenProvider.generateToken(authentication)

        response.status = HttpServletResponse.SC_OK
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = StandardCharsets.UTF_8.name()

        val loginResponse = AuthTokenResponseDto(accessToken)

        response.writer.write(objectMapper.writeValueAsString(loginResponse))
    }
}
