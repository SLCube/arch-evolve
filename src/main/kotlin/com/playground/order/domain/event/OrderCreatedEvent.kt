package com.playground.order.domain.event

data class OrderCreatedEvent(
    val products: List<OrderProductDetail>
) {
    data class OrderProductDetail(
        val productId: Long,
        val quantity: Int
    )
}