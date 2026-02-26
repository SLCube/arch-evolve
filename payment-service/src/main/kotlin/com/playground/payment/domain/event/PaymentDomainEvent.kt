package com.playground.payment.domain.event

import java.time.LocalDateTime
import java.util.UUID

sealed interface PaymentDomainEvent {
    val eventId: UUID
    val orderId: Long
    val userId: Long
    val occurredAt: LocalDateTime
}
