package com.playground.order.application.service

import com.playground.order.application.port.inbound.OrderOutboxEventUseCase
import com.playground.order.application.port.outbound.OrderEventPublisherPort
import com.playground.order.application.port.outbound.OutboxCommandPort
import com.playground.order.application.port.outbound.OutboxQueryPort
import com.playground.order.domain.outbox.OutboxStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderOutboxEventService(
    private val outboxQueryPort: OutboxQueryPort,
    private val outboxCommandPort: OutboxCommandPort,
    private val orderEventPublisherPort: OrderEventPublisherPort,
) : OrderOutboxEventUseCase {
    companion object {
        private const val POLL_LIMIT = 1000
    }

    @Transactional
    override fun pollAndPublishEvents() {
        val pendingEvents = outboxQueryPort.findByStatus(OutboxStatus.PENDING, POLL_LIMIT)
        if (pendingEvents.isEmpty()) return

        val published = orderEventPublisherPort.publishAll(pendingEvents)
        if (published.isNotEmpty()) {
            outboxCommandPort.bulkMarkAsPublished(published.map { it.id!! })
        }
    }
}
