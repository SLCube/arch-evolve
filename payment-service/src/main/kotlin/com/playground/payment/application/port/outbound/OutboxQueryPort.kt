package com.playground.payment.application.port.outbound

import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.domain.outbox.PaymentEventOutbox

fun interface OutboxQueryPort {
    fun findByStatus(status: OutboxStatus): List<PaymentEventOutbox>
}
