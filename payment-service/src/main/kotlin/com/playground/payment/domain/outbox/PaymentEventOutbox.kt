package com.playground.payment.domain.outbox

import java.time.LocalDateTime
import java.util.UUID

class PaymentEventOutbox(
    val id: Long? = null,
    val eventId: UUID,
    val eventType: OutboxEventType,
    val payload: String,
    val status: OutboxStatus,
    val occurredAt: LocalDateTime,
)
