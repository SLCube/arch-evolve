package com.playground.payment.domain.event

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class PaymentAuthorizedEvent(
    override val eventId: UUID,
    override val orderId: Long,
    override val userId: Long,
    val amount: BigDecimal,
    val pgTransactionId: String,
    override val occurredAt: LocalDateTime,
) : PaymentDomainEvent
