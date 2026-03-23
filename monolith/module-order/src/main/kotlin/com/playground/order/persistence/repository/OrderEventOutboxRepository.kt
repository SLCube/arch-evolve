package com.playground.order.persistence.repository

import com.playground.order.domain.outbox.OutboxStatus
import com.playground.order.persistence.entity.OrderEventOutboxJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OrderEventOutboxRepository : JpaRepository<OrderEventOutboxJpaEntity, Long> {
    @Query(
        value = "SELECT * FROM order_event_outbox WHERE status = :status ORDER BY outbox_id LIMIT :limit FOR UPDATE SKIP LOCKED",
        nativeQuery = true,
    )
    fun findByStatusWithLock(
        @Param("status") status: String,
        @Param("limit") limit: Int,
    ): List<OrderEventOutboxJpaEntity>

    @Modifying(clearAutomatically = true)
    @Query("UPDATE OrderEventOutboxJpaEntity o SET o.status = :status WHERE o.id IN :ids")
    fun bulkUpdateStatus(
        ids: List<Long>,
        status: OutboxStatus,
    )

    @Modifying(clearAutomatically = true)
    @Query("UPDATE OrderEventOutboxJpaEntity o SET o.retryCount = o.retryCount + 1 WHERE o.id IN :ids")
    fun bulkIncrementRetryCount(
        @Param("ids") ids: List<Long>,
    )
}
