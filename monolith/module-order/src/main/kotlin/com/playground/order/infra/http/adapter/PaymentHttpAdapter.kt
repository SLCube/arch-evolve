package com.playground.order.infra.http.adapter

import com.playground.order.application.port.outbound.PaymentPort
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.math.BigDecimal

@Component
class PaymentHttpAdapter(
    @Value("\${payment-service.url}") private val paymentServiceUrl: String,
) : PaymentPort {
    private val restClient = RestClient.create(paymentServiceUrl)

    override fun authorize(userId: Long, orderId: Long, amount: BigDecimal): String {
        val response = restClient.post()
            .uri("/internal/payments/authorize")
            .body(AuthorizeRequest(userId, orderId, amount))
            .retrieve()
            .body(AuthorizeResponse::class.java)!!

        return response.pgTransactionId
    }

    private data class AuthorizeRequest(
        val userId: Long,
        val orderId: Long,
        val amount: BigDecimal,
    )

    private data class AuthorizeResponse(
        val pgTransactionId: String,
    )
}
