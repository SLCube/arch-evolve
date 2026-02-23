package com.playground.payment.infra.kafka.event

import java.time.LocalDateTime

data class PaymentFailedEvent(
    val orderId: Long,
    val userId: Long,
    val failReason: String?,
    val occurredAt: LocalDateTime = LocalDateTime.now(),
)
