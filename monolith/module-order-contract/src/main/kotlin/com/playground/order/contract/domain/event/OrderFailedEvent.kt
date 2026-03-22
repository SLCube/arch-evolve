package com.playground.order.contract.domain.event

import com.playground.common.event.DomainEvent
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class OrderFailedEvent(
    val eventId: UUID,
    val orderId: Long,
    val userId: Long,
    val products: List<OrderProductDetail>,
    val totalAmount: BigDecimal,
    val occurredAt: LocalDateTime,
) : DomainEvent {
    data class OrderProductDetail(
        val productId: Long,
        val quantity: Int,
    )
    companion object
}
