package com.playground.payment.domain.event

import java.time.LocalDateTime
import java.util.UUID

data class PaymentFailedEvent(
    override val eventId: UUID,
    override val orderId: Long,
    override val userId: Long,
    val failReason: String,
    override val occurredAt: LocalDateTime,
) : PaymentDomainEvent
