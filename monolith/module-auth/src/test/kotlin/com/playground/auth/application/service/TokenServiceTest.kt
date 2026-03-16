package com.playground.auth.application.service

import com.playground.auth.application.port.outbound.RefreshTokenPort
import com.playground.auth.contract.security.AuthUserDetails
import com.playground.auth.domain.exception.InvalidRefreshTokenException
import com.playground.auth.domain.exception.RefreshTokenMismatchException
import com.playground.auth.domain.exception.RefreshTokenNotFoundException
import com.playground.auth.jwt.JwtProperties
import com.playground.auth.jwt.JwtTokenProvider
import com.playground.auth.utils.TokenHasher
import com.playground.user.contract.application.port.outbound.UserInfoQueryPort
import com.playground.user.contract.domain.vo.UserInfo
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
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

    private val jwtProperties: JwtProperties = JwtProperties(
        secret = "dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tdGVzdC1wdXJwb3NlLW9ubHktZG8tbm90LXVzZS1pbi1wcm9kdWN0aW9u",
        expirationHours = 1,
        refreshExpirationHours = 168,
    )
    private val jwtTokenProvider = JwtTokenProvider(jwtProperties = jwtProperties)
    private val refreshTokenPort: RefreshTokenPort = mock()
    private val userInfoQueryPort: UserInfoQueryPort = mock()
    private val tokenService = TokenService(
        jwtTokenProvider = jwtTokenProvider,
        jwtProperties = jwtProperties,
        refreshTokenPort = refreshTokenPort,
        userInfoQueryPort = userInfoQueryPort,
    )

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

        val response = tokenService.issueTokens(authentication)

        // 실제 JWT 토큰이 발급되었는지 확인
        jwtTokenProvider.validateToken(response.accessToken) shouldBe true
        jwtTokenProvider.validateToken(response.refreshToken) shouldBe true

        // RefreshToken이 해시되어 저장되었는지 확인
        verify(refreshTokenPort).save(
            userId = 1L,
            refreshToken = TokenHasher.hash(response.refreshToken),
            ttl = Duration.ofHours(168),
        )
    }

    @Test
    fun `refresh - 유효한 RefreshToken으로 새로운 토큰을 발급한다`() {
        val loginId = "testUser"
        val userId = 1L
        val refreshToken = jwtTokenProvider.generateRefreshToken(loginId)

        val userInfo = UserInfo(
            userId = userId,
            loginId = loginId,
            password = "encodedPassword",
            role = "USER",
        )

        given(userInfoQueryPort.getUserInfoByLoginId(loginId))
            .willReturn(userInfo)
        given(refreshTokenPort.findByUserId(userId))
            .willReturn(TokenHasher.hash(refreshToken))

        val response = tokenService.refresh(refreshToken)

        // 실제 JWT 토큰이 발급되었는지 확인
        jwtTokenProvider.validateToken(response.accessToken) shouldBe true
        jwtTokenProvider.validateToken(response.refreshToken) shouldBe true

        verify(userInfoQueryPort).getUserInfoByLoginId(loginId)
        verify(refreshTokenPort).findByUserId(userId)
        verify(refreshTokenPort).save(
            userId = userId,
            refreshToken = TokenHasher.hash(response.refreshToken),
            ttl = Duration.ofHours(168),
        )
    }

    @Test
    fun `refresh - 만료된 RefreshToken이면 예외를 발생시킨다`() {
        val expiredToken = createExpiredRefreshToken("testUser")

        shouldThrow<InvalidRefreshTokenException> {
            tokenService.refresh(expiredToken)
        }
    }

    @Test
    fun `refresh - 저장된 토큰과 불일치하면 예외를 발생시킨다`() {
        val loginId = "testUser"
        val userId = 1L
        val providedToken = jwtTokenProvider.generateRefreshToken(loginId)
        val storedToken = jwtTokenProvider.generateRefreshToken("otherUser") // 다른 사용자 토큰

        val userInfo = UserInfo(
            userId = userId,
            loginId = loginId,
            password = "encodedPassword",
            role = "USER",
        )

        given(userInfoQueryPort.getUserInfoByLoginId(loginId))
            .willReturn(userInfo)
        given(refreshTokenPort.findByUserId(userId))
            .willReturn(TokenHasher.hash(storedToken))

        shouldThrow<RefreshTokenMismatchException> {
            tokenService.refresh(providedToken)
        }
    }

    @Test
    fun `refresh - 저장소에 RefreshToken이 없으면 예외를 발생시킨다`() {
        val loginId = "testUser"
        val userId = 1L
        val refreshToken = jwtTokenProvider.generateRefreshToken(loginId)

        val userInfo = UserInfo(
            userId = userId,
            loginId = loginId,
            password = "encodedPassword",
            role = "USER",
        )

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

        shouldThrow<InvalidRefreshTokenException> {
            tokenService.refresh(invalidToken)
        }
    }

    private fun createExpiredRefreshToken(loginId: String): String {
        val now = Instant.now()
        val expiration = now.minus(1, ChronoUnit.HOURS)

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

    @Test
    fun `logout - RefreshToken을 삭제한다`() {
        val userId = 1L

        tokenService.logout(userId)

        verify(refreshTokenPort).deleteByUserId(userId)
    }
}
