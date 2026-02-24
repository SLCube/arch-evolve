package com.playground.payment.infra.kafka.consumer

import java.math.BigDecimal
import java.util.UUID

data class OrderCreatedEvent(
    val eventId: UUID,
    val orderId: Long,
    val userId: Long,
    val products: List<OrderProductDetail>,
    val totalAmount: BigDecimal,
) {
    data class OrderProductDetail(
        val productId: Long,
        val quantity: Int,
    )
}
