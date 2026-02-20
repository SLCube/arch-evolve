package com.playground.gateway.jwt

import com.playground.gateway.log.utils.logger
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import org.springframework.stereotype.Component
import javax.crypto.SecretKey

@Component
class JwtProvider(
    jwtProperties: JwtProperties,
) {
    private val log = logger()

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
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

    fun getUserId(token: String): String = (parseClaims(token)["userId"] as Number).toLong().toString()

    private fun parseClaims(token: String): Claims =
        Jwts
            .parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
}
