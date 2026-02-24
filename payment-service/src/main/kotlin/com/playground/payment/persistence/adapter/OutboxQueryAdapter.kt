package com.playground.payment.persistence.adapter

import com.playground.payment.application.port.outbound.OutboxQueryPort
import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.domain.outbox.PaymentEventOutbox
import com.playground.payment.persistence.mapper.toDomain
import com.playground.payment.persistence.repository.PaymentEventOutboxRepository
import org.springframework.stereotype.Component

@Component
class OutboxQueryAdapter(
    private val paymentEventOutboxRepository: PaymentEventOutboxRepository,
) : OutboxQueryPort {
    override fun findByStatus(status: OutboxStatus): List<PaymentEventOutbox> =
        paymentEventOutboxRepository.findByStatus(status).map { it.toDomain() }
}
