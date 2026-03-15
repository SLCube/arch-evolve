package com.playground.order.persistence.adapter

import com.playground.order.application.port.outbound.OutboxQueryPort
import com.playground.order.domain.outbox.OrderEventOutbox
import com.playground.order.domain.outbox.OutboxStatus
import com.playground.order.persistence.mapper.toDomain
import com.playground.order.persistence.repository.OrderEventOutboxRepository
import org.springframework.stereotype.Component

@Component
class OutboxQueryAdapter(
    private val orderEventOutboxRepository: OrderEventOutboxRepository,
) : OutboxQueryPort {
    override fun findByStatus(
        status: OutboxStatus,
        limit: Int,
    ): List<OrderEventOutbox> =
        orderEventOutboxRepository.findByStatusWithLock(status.name, limit).map { it.toDomain() }
}
