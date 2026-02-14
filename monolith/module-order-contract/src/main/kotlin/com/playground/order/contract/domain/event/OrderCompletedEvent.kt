package com.playground.order.contract.domain.event

import com.playground.common.event.DomainEvent
import java.math.BigDecimal

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
) : DomainEvent {
    data class OrderProductDetail(
        val productId: Long,
        val quantity: Int,
    )
    companion object
}
