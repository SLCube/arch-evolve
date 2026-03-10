package com.playground.delivery.consumer.event

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class OrderCompletedEvent(
    val userId: Long,
    val orderId: Long,
    val products: List<OrderProductDetail>,
    val totalAmount: BigDecimal,
    val receiverName: String,
    val receiverPhoneNumber: String,
    val zipCode: String,
    val baseAddress: String,
    val detailAddress: String,
    val occurredAt: LocalDateTime,
    val eventId: UUID,
) {
    data class OrderProductDetail(
        val productId: Long,
        val quantity: Int,
    )
}
