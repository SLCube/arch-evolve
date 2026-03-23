package com.playground.payment.domain.outbox

import java.time.LocalDateTime
import java.util.UUID

class PaymentEventOutbox(
    val id: Long? = null,
    val eventId: UUID,
    val orderId: Long,
    val eventType: OutboxEventType,
    val payload: String,
    val status: OutboxStatus,
    val occurredAt: LocalDateTime,
    val traceparent: String? = null,
    val retryCount: Int = 0,
)
