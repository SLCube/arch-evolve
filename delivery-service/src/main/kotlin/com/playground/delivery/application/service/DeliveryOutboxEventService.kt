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
        private const val MAX_ATTEMPTS = 3
    }

    @Transactional
    override fun pollAndPublishEvents() {
        val pendingEvents = outboxQueryPort.findByStatus(OutboxStatus.PENDING, POLL_LIMIT)
        if (pendingEvents.isEmpty()) return

        val published = deliveryEventPublisherPort.publishAll(pendingEvents)
        if (published.isNotEmpty()) {
            outboxCommandPort.bulkMarkAsPublished(published.map { it.id!! })
        }

        val publishedIds = published.map { it.id!! }.toSet()
        val failed = pendingEvents.filter { it.id!! !in publishedIds }
        if (failed.isEmpty()) return

        val (overLimit, underLimit) = failed.partition { it.retryCount + 1 >= MAX_ATTEMPTS }
        if (overLimit.isNotEmpty()) outboxCommandPort.bulkMarkAsFailed(overLimit.map { it.id!! })
        if (underLimit.isNotEmpty()) outboxCommandPort.bulkIncrementRetryCount(underLimit.map { it.id!! })
    }
}
