package com.playground.payment.persistence.repository

import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.persistence.entity.PaymentEventOutboxJpaEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface PaymentEventOutboxRepository : JpaRepository<PaymentEventOutboxJpaEntity, Long> {
    fun findByStatus(
        status: OutboxStatus,
        pageable: Pageable,
    ): List<PaymentEventOutboxJpaEntity>

    @Modifying
    @Query("UPDATE PaymentEventOutboxJpaEntity o SET o.status = :status WHERE o.id IN :ids")
    fun bulkUpdateStatus(
        ids: List<Long>,
        status: OutboxStatus,
    )
}
