package com.playground.auth.application.port.inbound

import com.playground.auth.presentation.response.AuthTokenResponseDto
import org.springframework.security.core.Authentication

interface TokenIssueUseCase {
    fun issueTokens(authentication: Authentication): AuthTokenResponseDto
}
