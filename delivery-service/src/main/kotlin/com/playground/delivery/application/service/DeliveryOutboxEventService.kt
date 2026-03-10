package com.playground.delivery.application.service

import com.playground.delivery.application.port.inbound.DeliveryOutboxEventUseCase
import com.playground.delivery.application.port.outbound.DeliveryEventPublisherPort
import com.playground.delivery.application.port.outbound.OutboxCommandPort
import com.playground.delivery.application.port.outbound.OutboxQueryPort
import com.playground.delivery.domain.outbox.OutboxStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeliveryOutboxEventService(
    private val outboxQueryPort: OutboxQueryPort,
    private val outboxCommandPort: OutboxCommandPort,
    private val deliveryEventPublisherPort: DeliveryEventPublisherPort,
) : DeliveryOutboxEventUseCase {
    companion object {
        private const val POLL_LIMIT = 1000
    }

    @Transactional
    override fun pollAndPublishEvents() {
        val pendingEvents = outboxQueryPort.findByStatus(OutboxStatus.PENDING, POLL_LIMIT)
        if (pendingEvents.isEmpty()) return

        val published = deliveryEventPublisherPort.publishAll(pendingEvents)
        if (published.isNotEmpty()) {
            outboxCommandPort.bulkMarkAsPublished(published.map { it.id!! })
        }
    }
}
