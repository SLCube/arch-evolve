package com.playground.auth.jwt

import com.playground.auth.contract.security.AuthUserDetails
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Suppress("NonAsciiCharacters")
class JwtTokenProviderTest {

    private val userDetailsService: UserDetailsService = mock()

    private lateinit var jwtTokenProvider: JwtTokenProvider

    private val jwtProperties =
        JwtProperties(
            secret = "YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFh", // 32 chars base64-like string
            expirationHours = 1,
        )

    @BeforeEach
    fun setUp() {
        jwtTokenProvider = JwtTokenProvider(jwtProperties, userDetailsService)
    }

    @Test
    fun `generateToken 은 사용자 정보와 권한을 포함한 JWT 를 생성한다`() {
        val authentication: Authentication = mock()
        val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("ROLE_ADMIN"))

        given(authentication.name).willReturn("tester")
        given(authentication.authorities).willReturn(authorities)

        val token = jwtTokenProvider.generateAccessToken(authentication)

        token.shouldNotBe("")

        val claims =
            Jwts
                .parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray()))
                .build()
                .parseSignedClaims(token)
                .payload

        claims.subject shouldBe "tester"
        claims["auth"].toString() shouldContain "ROLE_USER"
        claims["auth"].toString() shouldContain "ROLE_ADMIN"
    }

    @Test
    fun `getAuthentication 은 토큰의 subject 로 UserDetails 를 조회해 Authentication 을 반환한다`() {
        val authorities = listOf("ROLE_USER")
        val authUserDetails =
            AuthUserDetails(
                userId = 1L,
                loginId = "tester",
                password = "encodedPassword",
                roles = authorities,
            )

        given(userDetailsService.loadUserByUsername("tester"))
            .willReturn(authUserDetails)

        val token = createToken("tester", authorities)

        val authentication = jwtTokenProvider.getAuthentication(token)

        verify(userDetailsService).loadUserByUsername("tester")
        authentication.principal shouldBe authUserDetails
        authentication.authorities.map { it.authority } shouldBe listOf("ROLE_USER")
    }

    @Test
    fun `validateToken 은 유효한 토큰이면 true 를 반환한다`() {
        val token = createToken("tester", listOf("ROLE_USER"))

        jwtTokenProvider.validateToken(token) shouldBe true
    }

    @Test
    fun `validateToken 은 만료된 토큰이면 false 를 반환한다`() {
        val expiredToken = createToken("tester", listOf("ROLE_USER"), expired = true)

        jwtTokenProvider.validateToken(expiredToken) shouldBe false
    }

    private fun createToken(
        subject: String,
        roles: List<String>,
        expired: Boolean = false,
    ): String {
        val now = Instant.now()
        val expiration =
            if (expired) {
                now.minus(1, ChronoUnit.HOURS)
            } else {
                now.plus(1, ChronoUnit.HOURS)
            }

        return Jwts
            .builder()
            .subject(subject)
            .claim("auth", roles.joinToString(","))
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray()))
            .compact()
    }
}
