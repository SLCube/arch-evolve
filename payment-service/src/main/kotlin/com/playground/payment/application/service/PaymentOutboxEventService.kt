package com.playground.payment.application.service

import com.playground.payment.application.port.inbound.PaymentOutboxEventUseCase
import com.playground.payment.application.port.outbound.OutboxCommandPort
import com.playground.payment.application.port.outbound.OutboxQueryPort
import com.playground.payment.application.port.outbound.PaymentEventPublisherPort
import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.domain.outbox.PaymentEventOutbox
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentOutboxEventService(
    private val outboxQueryPort: OutboxQueryPort,
    private val outboxCommandPort: OutboxCommandPort,
    private val paymentEventPublisherPort: PaymentEventPublisherPort,
) : PaymentOutboxEventUseCase {
    companion object {
        private const val POLL_LIMIT = 1000
    }

    @Transactional(readOnly = true)
    override fun findPendingEvents(): List<PaymentEventOutbox> = outboxQueryPort.findByStatus(OutboxStatus.PENDING, POLL_LIMIT)

    @Transactional
    override fun publishEvents(outboxes: List<PaymentEventOutbox>) {
        val published = paymentEventPublisherPort.publishAll(outboxes)
        if (published.isNotEmpty()) {
            outboxCommandPort.bulkMarkAsPublished(published.map { it.id!! })
        }
    }
}
