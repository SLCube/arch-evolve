package com.playground.order.persistence.repository

import com.playground.order.domain.outbox.OutboxStatus
import com.playground.order.persistence.entity.OrderEventOutboxJpaEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface OrderEventOutboxRepository : JpaRepository<OrderEventOutboxJpaEntity, Long> {
    fun findByStatus(
        status: OutboxStatus,
        pageable: Pageable,
    ): List<OrderEventOutboxJpaEntity>

    @Modifying
    @Query("UPDATE OrderEventOutboxJpaEntity o SET o.status = :status WHERE o.id IN :ids")
    fun bulkUpdateStatus(
        ids: List<Long>,
        status: OutboxStatus,
    )
}
