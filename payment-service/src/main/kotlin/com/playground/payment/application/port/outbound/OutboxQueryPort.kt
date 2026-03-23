package com.playground.payment.application.port.outbound

import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.domain.outbox.PaymentEventOutbox

interface OutboxQueryPort {
    fun findByStatus(
        status: OutboxStatus,
        limit: Int,
    ): List<PaymentEventOutbox>

    fun countByStatus(status: OutboxStatus): Long
}
