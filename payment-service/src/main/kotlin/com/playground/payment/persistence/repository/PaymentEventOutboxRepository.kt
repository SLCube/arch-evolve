package com.playground.payment.persistence.repository

import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.persistence.entity.PaymentEventOutboxJpaEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentEventOutboxRepository : JpaRepository<PaymentEventOutboxJpaEntity, Long> {
    fun findByStatus(
        status: OutboxStatus,
        pageable: Pageable,
    ): List<PaymentEventOutboxJpaEntity>
}
