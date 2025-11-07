package com.playground.common.security.jwt

import com.playground.common.utils.logger
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.Collectors
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties,
    private val userDetailsService: UserDetailsService
) {

    private val log = logger()

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun generateToken(authentication: Authentication): String {
        val authorities = authentication.authorities.stream()
            .map { it: GrantedAuthority -> it.authority }
            .collect(Collectors.joining(","))

        val now = Instant.now()
        val expiration = now.plus(1, ChronoUnit.HOURS)

        return Jwts.builder()
            .subject(authentication.name)
            .claim("auth", authorities)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiration))
            .signWith(secretKey)
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)

        val authorities = claims["auth"]?.toString()?.split(",")
            ?.map { SimpleGrantedAuthority(it) }
            ?: emptyList()

        val userDetails = userDetailsService.loadUserByUsername(claims.subject)
        return UsernamePasswordAuthenticationToken(userDetails, "", authorities)
    }

    fun validateToken(token: String): Boolean {
        try {
            parseClaims(token)
            return true
        } catch (e: SecurityException) {
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

    private fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}