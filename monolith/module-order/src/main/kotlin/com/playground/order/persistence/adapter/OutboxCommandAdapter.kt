package com.playground.order.persistence.adapter

import com.playground.order.application.port.outbound.OutboxCommandPort
import com.playground.order.domain.outbox.OrderEventOutbox
import com.playground.order.domain.outbox.OutboxStatus
import com.playground.order.persistence.mapper.toDomain
import com.playground.order.persistence.mapper.toJpaEntity
import com.playground.order.persistence.repository.OrderEventOutboxRepository
import org.springframework.stereotype.Component

@Component
class OutboxCommandAdapter(
    private val orderEventOutboxRepository: OrderEventOutboxRepository,
) : OutboxCommandPort {
    override fun save(outbox: OrderEventOutbox): OrderEventOutbox =
        orderEventOutboxRepository.save(outbox.toJpaEntity()).toDomain()

    override fun bulkMarkAsPublished(ids: List<Long>) {
        orderEventOutboxRepository.bulkUpdateStatus(ids, OutboxStatus.PUBLISHED)
    }

    override fun bulkIncrementRetryCount(ids: List<Long>) {
        orderEventOutboxRepository.bulkIncrementRetryCount(ids)
    }

    override fun bulkMarkAsFailed(ids: List<Long>) {
        orderEventOutboxRepository.bulkUpdateStatus(ids, OutboxStatus.FAILED)
    }
}
