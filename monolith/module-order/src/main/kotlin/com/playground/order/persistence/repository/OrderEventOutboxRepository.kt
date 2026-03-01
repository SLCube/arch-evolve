package com.playground.order.persistence.repository

import com.playground.order.domain.outbox.OutboxStatus
import com.playground.order.persistence.entity.OrderEventOutboxJpaEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface OrderEventOutboxRepository : JpaRepository<OrderEventOutboxJpaEntity, Long> {
    fun findByStatus(
        status: OutboxStatus,
        pageable: Pageable,
    ): List<OrderEventOutboxJpaEntity>
}
