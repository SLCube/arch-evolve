package com.playground.delivery.persistence.repository

import com.playground.delivery.domain.outbox.OutboxStatus
import com.playground.delivery.persistence.entity.DeliveryEventOutboxJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface DeliveryEventOutboxRepository : JpaRepository<DeliveryEventOutboxJpaEntity, Long> {
    @Query(
        value = "SELECT * FROM delivery_event_outbox WHERE status = :status ORDER BY outbox_id LIMIT :limit FOR UPDATE SKIP LOCKED",
        nativeQuery = true,
    )
    fun findByStatusWithLock(
        @Param("status") status: String,
        @Param("limit") limit: Int,
    ): List<DeliveryEventOutboxJpaEntity>

    @Modifying(clearAutomatically = true)
    @Query("UPDATE DeliveryEventOutboxJpaEntity o SET o.status = :status WHERE o.id IN :ids")
    fun bulkUpdateStatus(
        ids: List<Long>,
        status: OutboxStatus,
    )

    @Modifying(clearAutomatically = true)
    @Query("UPDATE DeliveryEventOutboxJpaEntity o SET o.retryCount = o.retryCount + 1 WHERE o.id IN :ids")
    fun bulkIncrementRetryCount(
        @Param("ids") ids: List<Long>,
    )

    @Query("SELECT COUNT(o) FROM DeliveryEventOutboxJpaEntity o WHERE o.status = :status")
    fun countByStatus(
        @Param("status") status: OutboxStatus,
    ): Long
}
