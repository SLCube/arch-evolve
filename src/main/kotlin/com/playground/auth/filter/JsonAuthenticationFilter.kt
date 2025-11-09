package com.playground.auth.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.auth.presentation.request.AuthLoginRequestDto
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

class JsonAuthenticationFilter(
    private val objectMapper: ObjectMapper,
    authenticationManager: AuthenticationManager
): UsernamePasswordAuthenticationFilter(authenticationManager) {

    init {
        this.setFilterProcessesUrl("/users/login")
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {

        if (!request.contentType.equals("application/json", ignoreCase = true)) {
            return super.attemptAuthentication(request, response)
        }

        val loginRequest = objectMapper.readValue(request.inputStream, AuthLoginRequestDto::class.java)

        val authenticationToken = UsernamePasswordAuthenticationToken(loginRequest.loginId, loginRequest.password)

        return this.authenticationManager.authenticate(authenticationToken)
    }
}