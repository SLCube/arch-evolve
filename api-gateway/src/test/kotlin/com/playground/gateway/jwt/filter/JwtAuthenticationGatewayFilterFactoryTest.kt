package com.playground.gateway.jwt.filter

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import reactor.core.publisher.Mono
import com.playground.gateway.jwt.JwtProvider

@Suppress("NonAsciiCharacters")
class JwtAuthenticationGatewayFilterFactoryTest {
    private val jwtProvider = mock<JwtProvider>()
    private val filter = JwtAuthenticationGatewayFilterFactory(jwtProvider).apply(JwtAuthenticationGatewayFilterFactory.Config())

    @Test
    fun `Authorization 헤더가 없으면 401을 반환한다`() {
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/payment/payment-methods").build(),
        )
        val chain = mock<GatewayFilterChain>()

        filter.filter(exchange, chain).block()

        exchange.response.statusCode shouldBe HttpStatus.UNAUTHORIZED
    }

    @Test
    fun `Bearer prefix가 없으면 401을 반환한다`() {
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/payment/payment-methods")
                .header("Authorization", "Basic sometoken")
                .build(),
        )
        val chain = mock<GatewayFilterChain>()

        filter.filter(exchange, chain).block()

        exchange.response.statusCode shouldBe HttpStatus.UNAUTHORIZED
    }

    @Test
    fun `유효하지 않은 토큰이면 401을 반환한다`() {
        given(jwtProvider.validateToken("invalidtoken")).willReturn(false)
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/payment/payment-methods")
                .header("Authorization", "Bearer invalidtoken")
                .build(),
        )
        val chain = mock<GatewayFilterChain>()

        filter.filter(exchange, chain).block()

        exchange.response.statusCode shouldBe HttpStatus.UNAUTHORIZED
    }

    @Test
    fun `유효한 토큰이면 X-User-Id 헤더를 주입하고 다음 필터로 넘긴다`() {
        given(jwtProvider.validateToken("validtoken")).willReturn(true)
        given(jwtProvider.getUserId("validtoken")).willReturn("42")
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/payment/payment-methods")
                .header("Authorization", "Bearer validtoken")
                .build(),
        )
        val chain = mock<GatewayFilterChain>()
        given(chain.filter(any())).willReturn(Mono.empty())

        filter.filter(exchange, chain).block()

        verify(chain).filter(
            org.mockito.kotlin.argThat { mutatedExchange ->
                mutatedExchange.request.headers.getFirst("X-User-Id") == "42"
            },
        )
    }

    @Test
    fun `클라이언트가 X-User-Id 헤더를 직접 설정해도 Gateway에서 제거하고 올바른 값으로 교체한다`() {
        given(jwtProvider.validateToken("validtoken")).willReturn(true)
        given(jwtProvider.getUserId("validtoken")).willReturn("42")
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.get("/payment/payment-methods")
                .header("Authorization", "Bearer validtoken")
                .header("X-User-Id", "999")
                .build(),
        )
        val chain = mock<GatewayFilterChain>()
        given(chain.filter(any())).willReturn(Mono.empty())

        filter.filter(exchange, chain).block()

        verify(chain).filter(
            org.mockito.kotlin.argThat { mutatedExchange ->
                mutatedExchange.request.headers.getFirst("X-User-Id") == "42"
            },
        )
    }
}
