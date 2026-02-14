package com.playground.delivery.contract.domain.event

import com.playground.common.event.DomainEvent
import java.time.LocalDateTime

data class DeliveryCompletedEvent(
    val deliveryId: Long,
    val orderId: Long,
    val userId: Long,
    val deliveredAt: LocalDateTime,
) : DomainEvent {
    companion object
}

