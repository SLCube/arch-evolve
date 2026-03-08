package com.playground.gateway.filter

import com.playground.gateway.log.utils.logger
import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class MdcGlobalFilter :
    GlobalFilter,
    Ordered {
    private val log = logger()

    override fun filter(
        exchange: ServerWebExchange,
        chain: GatewayFilterChain,
    ): Mono<Void> {
        val request = exchange.request
        val startTime = System.currentTimeMillis()

        return chain
            .filter(exchange)
            .doFinally {
                val duration = System.currentTimeMillis() - startTime
                val status = exchange.response.statusCode?.value() ?: 0
                log.info(
                    "{} {} | {} | {}ms",
                    request.method,
                    request.uri.path,
                    status,
                    duration,
                )
            }
    }

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE
}
