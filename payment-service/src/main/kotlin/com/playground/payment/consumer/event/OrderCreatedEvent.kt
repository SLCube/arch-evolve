package com.playground.payment.consumer.event

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class OrderCreatedEvent(
    val eventId: UUID,
    val orderId: Long,
    val userId: Long,
    val products: List<OrderProductDetail>,
    val totalAmount: BigDecimal,
    val occurredAt: LocalDateTime,
) {
    data class OrderProductDetail(
        val productId: Long,
        val quantity: Int,
    )
}
