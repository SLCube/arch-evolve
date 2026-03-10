package com.playground.delivery.domain.outbox

import java.time.LocalDateTime
import java.util.UUID

class DeliveryEventOutbox(
    val id: Long? = null,
    val eventId: UUID,
    val orderId: Long,
    val eventType: OutboxEventType,
    val payload: String,
    val status: OutboxStatus,
    val occurredAt: LocalDateTime,
)
