package com.playground.payment.infra.kafka.event

import java.math.BigDecimal

data class OrderCreatedEvent(
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
