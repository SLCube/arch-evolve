package com.playground.order.consumer.event

import java.time.LocalDateTime
import java.util.UUID

data class PaymentFailedEvent(
    val eventId: UUID,
    val orderId: Long,
    val userId: Long,
    val failReason: String,
    val occurredAt: LocalDateTime,
)
