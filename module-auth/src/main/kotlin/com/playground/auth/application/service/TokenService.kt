package com.playground.auth.application.service

import com.playground.auth.application.port.inbound.TokenIssueUseCase
import com.playground.auth.application.port.inbound.TokenRefreshUseCase
import com.playground.auth.application.port.outbound.RefreshTokenPort
import com.playground.auth.contract.security.AuthUserDetails
import com.playground.auth.domain.exception.InvalidRefreshTokenException
import com.playground.auth.domain.exception.RefreshTokenMismatchException
import com.playground.auth.domain.exception.RefreshTokenNotFoundException
import com.playground.auth.jwt.JwtProperties
import com.playground.auth.jwt.JwtTokenProvider
import com.playground.auth.presentation.response.AuthTokenResponseDto
import com.playground.auth.utils.TokenHasher
import com.playground.user.contract.application.port.outbound.UserInfoQueryPort
import com.playground.user.contract.domain.vo.UserInfo
import io.jsonwebtoken.Claims
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class TokenService(
    private val jwtTokenProvider: JwtTokenProvider,
    private val jwtProperties: JwtProperties,
    private val refreshTokenPort: RefreshTokenPort,
    private val userInfoQueryPort: UserInfoQueryPort,
) : TokenIssueUseCase, TokenRefreshUseCase {

    override fun issueTokens(authentication: Authentication): AuthTokenResponseDto {
        val userDetails = authentication.principal as AuthUserDetails
        return generateAndSaveTokens(
            userId = userDetails.getUserId(),
            loginId = userDetails.username,
            authentication = authentication,
        )
    }

    override fun refresh(refreshToken: String): AuthTokenResponseDto {
        val claims = validateAndParseClaims(refreshToken)
        val loginId = extractLoginId(claims)
        val userInfo = userInfoQueryPort.getUserInfoByLoginId(loginId)

        verifyStoredToken(userInfo.userId, refreshToken)

        val authentication = createAuthentication(userInfo)
        return generateAndSaveTokens(
            userId = userInfo.userId,
            loginId = loginId,
            authentication = authentication,
        )
    }

    private fun generateAndSaveTokens(
        userId: Long,
        loginId: String,
        authentication: Authentication,
    ): AuthTokenResponseDto {
        val accessToken = jwtTokenProvider.generateAccessToken(authentication)
        val refreshToken = jwtTokenProvider.generateRefreshToken(loginId)

        refreshTokenPort.save(
            userId = userId,
            refreshToken = TokenHasher.hash(refreshToken),
            ttl = Duration.ofHours(jwtProperties.refreshExpirationHours),
        )

        return AuthTokenResponseDto(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    private fun validateAndParseClaims(refreshToken: String): Claims {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw InvalidRefreshTokenException()
        }
        return jwtTokenProvider.parseClaims(refreshToken)
    }

    private fun extractLoginId(claims: Claims): String =
        claims.subject ?: throw InvalidRefreshTokenException()

    private fun verifyStoredToken(userId: Long, providedToken: String) {
        val storedTokenHash = refreshTokenPort.findByUserId(userId)
            ?: throw RefreshTokenNotFoundException()

        if (storedTokenHash != TokenHasher.hash(providedToken)) {
            throw RefreshTokenMismatchException()
        }
    }

    private fun createAuthentication(userInfo: UserInfo): Authentication {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_${userInfo.role}"))
        return UsernamePasswordAuthenticationToken(userInfo.loginId, null, authorities)
    }
}
