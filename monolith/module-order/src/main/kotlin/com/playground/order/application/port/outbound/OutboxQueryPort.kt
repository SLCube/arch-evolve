package com.playground.order.application.port.outbound

import com.playground.order.domain.outbox.OrderEventOutbox
import com.playground.order.domain.outbox.OutboxStatus

fun interface OutboxQueryPort {
    fun findByStatus(
        status: OutboxStatus,
        limit: Int,
    ): List<OrderEventOutbox>
}
