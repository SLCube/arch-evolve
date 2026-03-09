package com.playground.payment.persistence.entity

import com.playground.payment.domain.outbox.OutboxEventType
import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.persistence.entity.base.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "payment_event_outbox")
class PaymentEventOutboxJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_id")
    val id: Long? = null,
    @Column(nullable = false, updatable = false)
    val eventId: UUID,
    @Column(nullable = false, updatable = false)
    val orderId: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val eventType: OutboxEventType,
    @Column(nullable = false, columnDefinition = "TEXT")
    val payload: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OutboxStatus,
    @Column(nullable = false, updatable = false)
    val occurredAt: LocalDateTime,
) : BaseEntity()
