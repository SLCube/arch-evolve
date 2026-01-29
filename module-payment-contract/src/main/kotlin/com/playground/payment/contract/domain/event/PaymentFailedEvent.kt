package com.playground.payment.contract.domain.event

import java.time.LocalDateTime

data class PaymentFailedEvent(
    val orderId: Long,
    val userId: Long,
    val failReason: String?,
    val occurAt: LocalDateTime = LocalDateTime.now(),
)
