package com.playground.payment.persistence.adapter

import com.playground.payment.application.port.outbound.OutboxCommandPort
import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.domain.outbox.PaymentEventOutbox
import com.playground.payment.persistence.mapper.toDomain
import com.playground.payment.persistence.mapper.toJpaEntity
import com.playground.payment.persistence.repository.PaymentEventOutboxRepository
import org.springframework.stereotype.Component

@Component
class OutboxCommandAdapter(
    private val paymentEventOutboxRepository: PaymentEventOutboxRepository,
) : OutboxCommandPort {
    override fun save(outbox: PaymentEventOutbox): PaymentEventOutbox = paymentEventOutboxRepository.save(outbox.toJpaEntity()).toDomain()

    override fun bulkMarkAsPublished(ids: List<Long>) {
        paymentEventOutboxRepository.bulkUpdateStatus(ids, OutboxStatus.PUBLISHED)
    }
}
