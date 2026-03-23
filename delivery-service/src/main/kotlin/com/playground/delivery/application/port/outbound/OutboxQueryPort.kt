package com.playground.delivery.application.port.outbound

import com.playground.delivery.domain.outbox.DeliveryEventOutbox
import com.playground.delivery.domain.outbox.OutboxStatus

interface OutboxQueryPort {
    fun findByStatus(
        status: OutboxStatus,
        limit: Int,
    ): List<DeliveryEventOutbox>

    fun countByStatus(status: OutboxStatus): Long
}
