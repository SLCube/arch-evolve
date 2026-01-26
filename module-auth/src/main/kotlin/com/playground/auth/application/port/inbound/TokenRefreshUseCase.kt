package com.playground.auth.application.port.inbound

import com.playground.auth.presentation.response.AuthTokenResponseDto

interface TokenRefreshUseCase {
    fun refresh(refreshToken: String): AuthTokenResponseDto
}
