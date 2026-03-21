package com.playground.gateway.jwt.filter

import com.playground.gateway.jwt.JwtProvider
import org.springframework.cloud.gateway.filter.GatewayFilter
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

@Component
class JwtAuthenticationGatewayFilterFactory(
    private val jwtProvider: JwtProvider,
) : AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config>(Config::class.java) {
    override fun apply(config: Config): GatewayFilter =
        GatewayFilter { exchange, chain ->
            val authHeader = exchange.request.headers.getFirst("Authorization")

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                return@GatewayFilter exchange.response.setComplete()
            }

            val token = authHeader.substring(7)

            if (!jwtProvider.validateToken(token)) {
                exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                return@GatewayFilter exchange.response.setComplete()
            }

            val userId = jwtProvider.getUserId(token)
            val mutatedRequest =
                exchange.request
                    .mutate()
                    .headers { it.remove("X-User-Id") }
                    .header("X-User-Id", userId)
                    .build()

            chain.filter(exchange.mutate().request(mutatedRequest).build())
        }

    class Config
}
