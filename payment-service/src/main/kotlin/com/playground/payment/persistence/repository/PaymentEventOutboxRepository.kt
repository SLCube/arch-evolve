package com.playground.payment.persistence.repository

import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.persistence.entity.PaymentEventOutboxJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PaymentEventOutboxRepository : JpaRepository<PaymentEventOutboxJpaEntity, Long> {
    @Query(
        value = "SELECT * FROM payment_event_outbox WHERE status = :status ORDER BY outbox_id LIMIT :limit FOR UPDATE SKIP LOCKED",
        nativeQuery = true,
    )
    fun findByStatusWithLock(
        @Param("status") status: String,
        @Param("limit") limit: Int,
    ): List<PaymentEventOutboxJpaEntity>

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PaymentEventOutboxJpaEntity o SET o.status = :status WHERE o.id IN :ids")
    fun bulkUpdateStatus(
        ids: List<Long>,
        status: OutboxStatus,
    )

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PaymentEventOutboxJpaEntity o SET o.retryCount = o.retryCount + 1 WHERE o.id IN :ids")
    fun bulkIncrementRetryCount(
        @Param("ids") ids: List<Long>,
    )
}
