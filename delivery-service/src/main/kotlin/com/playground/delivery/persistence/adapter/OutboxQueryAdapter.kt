package com.playground.delivery.persistence.adapter

import com.playground.delivery.application.port.outbound.OutboxQueryPort
import com.playground.delivery.domain.outbox.DeliveryEventOutbox
import com.playground.delivery.domain.outbox.OutboxStatus
import com.playground.delivery.persistence.mapper.toDomain
import com.playground.delivery.persistence.repository.DeliveryEventOutboxRepository
import org.springframework.stereotype.Component

@Component
class OutboxQueryAdapter(
    private val deliveryEventOutboxRepository: DeliveryEventOutboxRepository,
) : OutboxQueryPort {
    override fun findByStatus(
        status: OutboxStatus,
        limit: Int,
    ): List<DeliveryEventOutbox> = deliveryEventOutboxRepository.findByStatusWithLock(status.name, limit).map { it.toDomain() }

    override fun countByStatus(status: OutboxStatus): Long = deliveryEventOutboxRepository.countByStatus(status)
}
