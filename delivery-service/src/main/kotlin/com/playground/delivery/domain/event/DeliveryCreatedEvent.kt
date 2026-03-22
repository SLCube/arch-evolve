package com.playground.delivery.domain.event

import java.time.LocalDateTime
import java.util.UUID

data class DeliveryCreatedEvent(
    val eventId: UUID,
    val deliveryId: Long,
    val orderId: Long,
    val userId: Long,
    val occurredAt: LocalDateTime,
)
