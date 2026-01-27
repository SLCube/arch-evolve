package com.playground.auth.application.service

import com.playground.auth.application.port.outbound.RefreshTokenPort
import com.playground.auth.contract.security.AuthUserDetails
import com.playground.auth.domain.exception.InvalidRefreshTokenException
import com.playground.auth.domain.exception.RefreshTokenMismatchException
import com.playground.auth.domain.exception.RefreshTokenNotFoundException
import com.playground.auth.jwt.JwtProperties
import com.playground.auth.jwt.JwtTokenProvider
import com.playground.user.contract.application.port.outbound.UserInfoQueryPort
import com.playground.user.contract.domain.vo.UserInfo
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Suppress("NonAsciiCharacters")
class TokenServiceTest {

    private val jwtTokenProvider: JwtTokenProvider = mock()
    private val jwtProperties: JwtProperties = JwtProperties(
        secret = "c3ByaW5nLWJvb3Qtand0LXR1dG9yaWFsLXNlY3JldC1rZXktZm9yLWhzMjU2LWFsZ29yaXRobQo=",
        expirationHours = 1,
        refreshExpirationHours = 168,
    )
    private val refreshTokenPort: RefreshTokenPort = mock()
    private val userInfoQueryPort: UserInfoQueryPort = mock()

    private lateinit var tokenService: TokenService
    private lateinit var realJwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        tokenService = TokenService(
            jwtTokenProvider = jwtTokenProvider,
            jwtProperties = jwtProperties,
            refreshTokenPort = refreshTokenPort,
            userInfoQueryPort = userInfoQueryPort,
        )

        realJwtTokenProvider = JwtTokenProvider(
            jwtProperties = jwtProperties,
            userDetailsService = mock(),
        )
    }

    @Test
    fun `issueTokens - 로그인 성공 시 AccessToken과 RefreshToken을 발급하고 저장한다`() {
        val userDetails = AuthUserDetails(
            userId = 1L,
            loginId = "testUser",
            password = "encodedPassword",
            roles = listOf("USER"),
        )

        val authentication = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            listOf(SimpleGrantedAuthority("ROLE_USER")),
        )

        given(jwtTokenProvider.generateAccessToken(authentication))
            .willReturn("mock-access-token")
        given(jwtTokenProvider.generateRefreshToken(userDetails.username))
            .willReturn("mock-refresh-token")

        val response = tokenService.issueTokens(authentication)

        response.accessToken shouldBe "mock-access-token"
        response.refreshToken shouldBe "mock-refresh-token"

        verify(jwtTokenProvider).generateAccessToken(authentication)
        verify(jwtTokenProvider).generateRefreshToken(userDetails.username)
        verify(refreshTokenPort).save(
            userId = 1L,
            refreshToken = "mock-refresh-token",
            ttl = Duration.ofHours(168),
        )
    }

    @Test
    fun `refresh - 유효한 RefreshToken으로 새로운 토큰을 발급한다`() {
        val loginId = "testUser"
        val userId = 1L
        val refreshToken = createRealRefreshToken(loginId)

        val userInfo = UserInfo(
            userId = userId,
            loginId = loginId,
            password = "encodedPassword",
            role = "USER",
        )

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true)
        given(jwtTokenProvider.parseClaims(refreshToken))
            .willReturn(parseClaims(refreshToken))
        given(userInfoQueryPort.getUserInfoByLoginId(loginId))
            .willReturn(userInfo)
        given(refreshTokenPort.findByUserId(userId))
            .willReturn(refreshToken)
        given(jwtTokenProvider.generateAccessToken(any()))
            .willReturn("new-access-token")
        given(jwtTokenProvider.generateRefreshToken(loginId))
            .willReturn("new-refresh-token")

        val response = tokenService.refresh(refreshToken)

        response.accessToken shouldBe "new-access-token"
        response.refreshToken shouldBe "new-refresh-token"

        verify(userInfoQueryPort).getUserInfoByLoginId(loginId)
        verify(refreshTokenPort).findByUserId(userId)
        verify(refreshTokenPort).save(
            userId = userId,
            refreshToken = "new-refresh-token",
            ttl = Duration.ofHours(168),
        )
    }

    @Test
    fun `refresh - 만료된 RefreshToken이면 예외를 발생시킨다`() {
        val expiredToken = createRealRefreshToken("testUser", expired = true)

        given(jwtTokenProvider.validateToken(expiredToken)).willReturn(false)

        shouldThrow<InvalidRefreshTokenException> {
            tokenService.refresh(expiredToken)
        }
    }

    @Test
    fun `refresh - 저장된 토큰과 불일치하면 예외를 발생시킨다`() {
        val loginId = "testUser"
        val userId = 1L
        val providedToken = "provided-token"
        val storedToken = "stored-token" // 다른 토큰

        val userInfo = UserInfo(
            userId = userId,
            loginId = loginId,
            password = "encodedPassword",
            role = "USER",
        )

        val claims = mock<Claims>()
        given(claims.subject).willReturn(loginId)

        given(jwtTokenProvider.validateToken(providedToken)).willReturn(true)
        given(jwtTokenProvider.parseClaims(providedToken))
            .willReturn(claims)
        given(userInfoQueryPort.getUserInfoByLoginId(loginId))
            .willReturn(userInfo)
        given(refreshTokenPort.findByUserId(userId))
            .willReturn(storedToken)

        shouldThrow<RefreshTokenMismatchException> {
            tokenService.refresh(providedToken)
        }
    }

    @Test
    fun `refresh - 저장소에 RefreshToken이 없으면 예외를 발생시킨다`() {
        val loginId = "testUser"
        val userId = 1L
        val refreshToken = createRealRefreshToken(loginId)

        val userInfo = UserInfo(
            userId = userId,
            loginId = loginId,
            password = "encodedPassword",
            role = "USER",
        )

        given(jwtTokenProvider.validateToken(refreshToken)).willReturn(true)
        given(jwtTokenProvider.parseClaims(refreshToken))
            .willReturn(parseClaims(refreshToken))
        given(userInfoQueryPort.getUserInfoByLoginId(loginId))
            .willReturn(userInfo)
        given(refreshTokenPort.findByUserId(userId))
            .willReturn(null)

        shouldThrow<RefreshTokenNotFoundException> {
            tokenService.refresh(refreshToken)
        }
    }

    @Test
    fun `refresh - subject가 없는 토큰이면 예외를 발생시킨다`() {
        val invalidToken = createTokenWithoutSubject()

        given(jwtTokenProvider.validateToken(invalidToken)).willReturn(true)
        given(jwtTokenProvider.parseClaims(invalidToken))
            .willReturn(parseClaims(invalidToken))

        shouldThrow<InvalidRefreshTokenException> {
            tokenService.refresh(invalidToken)
        }
    }

    private fun createRealRefreshToken(loginId: String, expired: Boolean = false): String {
        val now = Instant.now()
        val expiration = if (expired) {
            now.minus(1, ChronoUnit.HOURS)
        } else {
            now.plus(7, ChronoUnit.DAYS)
        }

        return Jwts.builder()
            .subject(loginId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray()))
            .compact()
    }

    private fun createTokenWithoutSubject(): String {
        val now = Instant.now()
        return Jwts.builder()
            .claim("someKey", "someValue")
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(7, ChronoUnit.DAYS)))
            .signWith(Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray()))
            .compact()
    }

    private fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray()))
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
