package com.playground.delivery.contract.domain.event

import com.playground.common.event.DomainEvent

data class DeliveryCreatedEvent(
    val deliveryId: Long,
    val orderId: Long,
    val userId: Long,
) : DomainEvent {
    companion object
}

