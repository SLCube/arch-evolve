package com.playground.payment.contract.domain.event

import com.playground.common.event.DomainEvent
import java.math.BigDecimal
import java.time.LocalDateTime

data class PaymentCompletedEvent(
    val orderId: Long,
    val userId: Long,
    val amount: BigDecimal,
    val pgTransactionId: String,
    val occurAt: LocalDateTime = LocalDateTime.now()
): DomainEvent
