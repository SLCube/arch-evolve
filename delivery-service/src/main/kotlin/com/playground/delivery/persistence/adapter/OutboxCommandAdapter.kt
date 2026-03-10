package com.playground.delivery.persistence.adapter

import com.playground.delivery.application.port.outbound.OutboxCommandPort
import com.playground.delivery.domain.outbox.DeliveryEventOutbox
import com.playground.delivery.domain.outbox.OutboxStatus
import com.playground.delivery.persistence.mapper.toDomain
import com.playground.delivery.persistence.mapper.toJpaEntity
import com.playground.delivery.persistence.repository.DeliveryEventOutboxRepository
import org.springframework.stereotype.Component

@Component
class OutboxCommandAdapter(
    private val deliveryEventOutboxRepository: DeliveryEventOutboxRepository,
) : OutboxCommandPort {
    override fun save(outbox: DeliveryEventOutbox): DeliveryEventOutbox =
        deliveryEventOutboxRepository.save(outbox.toJpaEntity()).toDomain()

    override fun bulkMarkAsPublished(ids: List<Long>) {
        deliveryEventOutboxRepository.bulkUpdateStatus(ids, OutboxStatus.PUBLISHED)
    }
}
