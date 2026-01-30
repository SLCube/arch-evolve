package com.playground.order.contract.domain.event

import com.playground.common.event.DomainEvent
import java.math.BigDecimal

data class OrderFailedEvent(
    val orderId: Long,
    val userId: Long,
    val products: List<OrderProductDetail>,
    val totalAmount: BigDecimal,
) : DomainEvent {
    data class OrderProductDetail(
        val productId: Long,
        val quantity: Int,
    )
    companion object
}
