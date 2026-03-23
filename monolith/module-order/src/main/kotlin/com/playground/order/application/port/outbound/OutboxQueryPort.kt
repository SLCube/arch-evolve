package com.playground.order.application.port.outbound

import com.playground.order.domain.outbox.OrderEventOutbox
import com.playground.order.domain.outbox.OutboxStatus

interface OutboxQueryPort {
    fun findByStatus(
        status: OutboxStatus,
        limit: Int,
    ): List<OrderEventOutbox>

    fun countByStatus(status: OutboxStatus): Long
}
