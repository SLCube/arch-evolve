package com.playground.delivery.persistence.repository

import com.playground.delivery.domain.outbox.OutboxStatus
import com.playground.delivery.persistence.entity.DeliveryEventOutboxJpaEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface DeliveryEventOutboxRepository : JpaRepository<DeliveryEventOutboxJpaEntity, Long> {
    fun findByStatus(
        status: OutboxStatus,
        pageable: Pageable,
    ): List<DeliveryEventOutboxJpaEntity>

    @Modifying
    @Query("UPDATE DeliveryEventOutboxJpaEntity o SET o.status = :status WHERE o.id IN :ids")
    fun bulkUpdateStatus(
        ids: List<Long>,
        status: OutboxStatus,
    )
}
