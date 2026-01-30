package com.playground.payment.contract.domain.event

import com.playground.common.event.DomainEvent
import java.time.LocalDateTime

data class PaymentFailedEvent(
    val orderId: Long,
    val userId: Long,
    val failReason: String?,
    val occurAt: LocalDateTime = LocalDateTime.now(),
): DomainEvent
