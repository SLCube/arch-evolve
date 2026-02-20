package com.playground.gateway.filter

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.server.MockServerWebExchange
import reactor.core.publisher.Mono

@Suppress("NonAsciiCharacters")
class BlockInternalPathFilterTest {
    private val filter = BlockInternalPathFilter()

    @Test
    fun `internal 경로 요청은 403을 반환한다`() {
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/internal/payments/authorize").build(),
        )
        val chain = mock<GatewayFilterChain>()

        filter.filter(exchange, chain).block()

        exchange.response.statusCode shouldBe HttpStatus.FORBIDDEN
    }

    @Test
    fun `internal이 아닌 경로는 다음 필터로 넘긴다`() {
        val exchange = MockServerWebExchange.from(
            MockServerHttpRequest.post("/payment/payment-methods").build(),
        )
        val chain = mock<GatewayFilterChain>()
        given(chain.filter(exchange)).willReturn(Mono.empty())

        filter.filter(exchange, chain).block()

        verify(chain).filter(exchange)
    }
}
