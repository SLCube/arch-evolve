package com.playground.order.contract.domain.event

import com.playground.common.event.DomainEvent

data class OrderCreatedEvent(
    val orderId: Long,
    val userId: Long,
    val products: List<OrderProductDetail>,
) : DomainEvent {
    data class OrderProductDetail(
        val productId: Long,
        val quantity: Int,
    )
}
