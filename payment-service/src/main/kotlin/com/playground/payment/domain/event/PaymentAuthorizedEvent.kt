package com.playground.payment.domain.event

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class PaymentAuthorizedEvent(
    val eventId: UUID,
    val orderId: Long,
    val userId: Long,
    val amount: BigDecimal,
    val pgTransactionId: String,
    val occurredAt: LocalDateTime,
)
