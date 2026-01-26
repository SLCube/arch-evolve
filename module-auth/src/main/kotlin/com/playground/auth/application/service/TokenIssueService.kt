package com.playground.auth.application.service

import com.playground.auth.application.port.inbound.TokenIssueUseCase
import com.playground.auth.application.port.outbound.RefreshTokenPort
import com.playground.auth.contract.security.AuthUserDetails
import com.playground.auth.jwt.JwtProperties
import com.playground.auth.jwt.JwtTokenProvider
import com.playground.auth.presentation.response.AuthTokenResponseDto
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class TokenIssueService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
    private val refreshTokenPort: RefreshTokenPort,
) : TokenIssueUseCase {
    override fun issueTokens(authentication: Authentication): AuthTokenResponseDto {
        val userDetails = authentication.principal as AuthUserDetails
        val userId = userDetails.getUserId()
        val loginId = userDetails.username

        val accessToken = jwtTokenProvider.generateAccessToken(authentication)
        val refreshToken = jwtTokenProvider.generateRefreshToken(loginId)

        refreshTokenPort.save(
            userId = userId,
            refreshToken = refreshToken,
            ttl = Duration.ofHours(jwtProperties.refreshExpirationHours),
        )

        return AuthTokenResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }
}
