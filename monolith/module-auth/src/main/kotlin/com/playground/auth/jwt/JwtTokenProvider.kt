package com.playground.auth.jwt

import com.playground.auth.contract.security.AuthUserDetails
import com.playground.common.log.utils.logger
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
) {
    private val log = logger()

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun generateAccessToken(authentication: Authentication): String {
        val authorities = authentication.authorities.joinToString(",") { it.authority }

        val now = Instant.now()
        val expiration = now.plus(jwtProperties.expirationHours, ChronoUnit.HOURS)

        val userId = (authentication.principal as AuthUserDetails).getUserId()

        return Jwts
            .builder()
            .subject(authentication.name)
            .claim("auth", authorities)
            .claim("userId", userId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(loginId: String): String {
        val now = Instant.now()
        val expiration = now.plus(jwtProperties.refreshExpirationHours, ChronoUnit.HOURS)

        return Jwts
            .builder()
            .subject(loginId)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)

        val authClaims = claims["auth"]?.toString()?.split(",") ?: emptyList()

        val roles = authClaims.map { it.removePrefix("ROLE_") }

        val userId =
            (claims["userId"] as Number?)?.toLong()
                ?: throw IllegalArgumentException("userId claim is missing")
        val loginId = claims.subject
        val userDetails = AuthUserDetails(userId, loginId, "", roles)

        return UsernamePasswordAuthenticationToken(userDetails, "", userDetails.authorities)
    }

    fun validateToken(token: String): Boolean {
        try {
            parseClaims(token)
            return true
        } catch (e: SignatureException) {
            log.error("Invalid JWT Signature : ", e)
        } catch (e: MalformedJwtException) {
            log.error("Invalid JWT token : ", e)
        } catch (e: ExpiredJwtException) {
            log.warn("Expired JWT token : ", e)
        } catch (e: UnsupportedJwtException) {
            log.error("Unsupported JWT token : ", e)
        } catch (e: IllegalArgumentException) {
            log.error("JWT claims string is empty : ", e)
        }
        return false
    }

    fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
