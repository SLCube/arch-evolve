package com.playground.gateway.filter

import com.playground.gateway.log.utils.logger
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class MdcGlobalFilter :
    GlobalFilter,
    Ordered {
    private val log = logger()

    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
    ): Mono<Void> {
        val requestId = UUID.randomUUID().toString()
        val request = exchange.request
        val startTime = System.currentTimeMillis()

        val mutatedRequest =
            request
                .mutate()
                .header("X-Request-Id", requestId)
                .build()

        val mutatedExchange =
            exchange.mutate().request(mutatedRequest).build()

        return chain
            .filter(mutatedExchange)
            .doFinally {
                val duration = System.currentTimeMillis() - startTime
                val status = mutatedExchange.response.statusCode?.value() ?: 0
                log.info(
                    "[{}] {} {} | {} | {}ms",
                    requestId,
                    request.method,
                    request.uri.path,
                    status,
                    duration,
                )
            }
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}
