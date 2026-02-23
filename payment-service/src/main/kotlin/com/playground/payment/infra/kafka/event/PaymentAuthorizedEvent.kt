package com.playground.payment.infra.kafka.event

import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentAuthorizedEvent(
    val orderId: Long,
    val userId: Long,
    val amount: BigDecimal,
    val pgTransactionId: String,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
)
