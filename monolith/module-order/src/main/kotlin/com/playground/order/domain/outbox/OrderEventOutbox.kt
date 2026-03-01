package com.playground.order.domain.outbox

import java.time.LocalDateTime
import java.util.UUID

class OrderEventOutbox(
    val id: Long? = null,
    val eventId: UUID,
    val eventType: OutboxEventType,
    val payload: String,
    var status: OutboxStatus,
    val occurredAt: LocalDateTime,
    val requestId: String,
) {
    fun markAsPublished() {
        this.status = OutboxStatus.PUBLISHED
    }
}
