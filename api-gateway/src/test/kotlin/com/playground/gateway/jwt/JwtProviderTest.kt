package com.playground.gateway.jwt

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@Suppress("NonAsciiCharacters")
class JwtProviderTest {
    private lateinit var jwtProvider: JwtProvider

    private val jwtProperties =
        JwtProperties(
            secret = "YWFhYWFhYWFhYWFhYWFhYWFhYWFhYWFh",
        )

    @BeforeEach
    fun setUp() {
        jwtProvider = JwtProvider(jwtProperties)
    }

    @Test
    fun `validateToken 은 유효한 토큰이면 true 를 반환한다`() {
        val token = createToken("1")

        jwtProvider.validateToken(token) shouldBe true
    }

    @Test
    fun `validateToken 은 만료된 토큰이면 false 를 반환한다`() {
        val token = createToken("1", expired = true)

        jwtProvider.validateToken(token) shouldBe false
    }

    @Test
    fun `validateToken 은 변조된 토큰이면 false 를 반환한다`() {
        val token = createToken("1", tampered = true)

        jwtProvider.validateToken(token) shouldBe false
    }

    @Test
    fun `getUserId 는 토큰에서 subject 를 추출한다`() {
        val token = createToken("1")

        jwtProvider.getUserId(token) shouldBe "1"
    }

    private fun createToken(
        subject: String,
        expired: Boolean = false,
        tampered: Boolean = false,
    ): String {
        val now = Instant.now()
        val expiration =
            if (expired) {
                now.minus(1, ChronoUnit.HOURS)
            } else {
                now.plus(1, ChronoUnit.HOURS)
            }
        val key =
            if (tampered) {
                "WldsbGJXRmhiV0ZoYldGaGJXRmhiV0Zo" // 다른 키
            } else {
                jwtProperties.secret
            }

        return Jwts
            .builder()
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(Keys.hmacShaKeyFor(key.toByteArray()))
            .compact()
    }
}
