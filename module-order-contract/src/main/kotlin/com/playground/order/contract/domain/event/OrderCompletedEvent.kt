package com.playground.order.contract.domain.event

import com.playground.common.event.DomainEvent
import java.math.BigDecimal

data class OrderCompletedEvent(
    val userId: Long,
    val orderId: Long,
    val totalAmount: BigDecimal,

) : DomainEvent {
}