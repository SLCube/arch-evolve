package com.playground.auth.application.service

import com.playground.auth.application.port.inbound.TokenRefreshUseCase
import com.playground.auth.application.port.outbound.RefreshTokenPort
import com.playground.auth.jwt.JwtProperties
import com.playground.auth.jwt.JwtTokenProvider
import com.playground.auth.presentation.response.AuthTokenResponseDto
import com.playground.user.contract.application.port.outbound.UserInfoQueryPort
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class TokenRefreshService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
    private val refreshTokenPort: RefreshTokenPort,
    private val userInfoQueryPort: UserInfoQueryPort,
) : TokenRefreshUseCase {
    override fun refresh(refreshToken: String): AuthTokenResponseDto {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw IllegalArgumentException("Invalid or expired refresh token")
        }

        val claims = jwtTokenProvider.parseClaims(refreshToken)
        val loginId = claims.subject
            ?: throw IllegalArgumentException("Invalid token format")

        val userInfo = userInfoQueryPort.getUserInfoByLoginId(loginId)
        val userId = userInfo.userId

        val storedToken = refreshTokenPort.findByUserId(userId)
            ?: throw IllegalArgumentException("Refresh token not found")

        if (storedToken != refreshToken) {
            throw IllegalArgumentException("Token mismatch")
        }

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${userInfo.role}"))
        val authentication = UsernamePasswordAuthenticationToken(userInfo.loginId, null, authorities)

        val newAccessToken = jwtTokenProvider.generateAccessToken(authentication)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(loginId)

        refreshTokenPort.save(
            userId = userId,
            refreshToken = newRefreshToken,
            ttl = Duration.ofHours(jwtProperties.refreshExpirationHours),
        )

        return AuthTokenResponseDto(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
        )
    }
}
