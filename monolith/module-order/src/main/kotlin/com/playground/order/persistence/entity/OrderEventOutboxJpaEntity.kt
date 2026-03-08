package com.playground.order.persistence.entity

import com.playground.common.persistence.jpa.BaseEntity
import com.playground.order.domain.outbox.OutboxEventType
import com.playground.order.domain.outbox.OutboxStatus
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
@Table(name = "order_event_outbox")
class OrderEventOutboxJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outbox_id")
    val id: Long? = null,
    @Column(nullable = false, updatable = false)
    val eventId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    val eventType: OutboxEventType,
    @Column(nullable = false, updatable = false, columnDefinition = "TEXT")
    val payload: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: OutboxStatus,
    @Column(nullable = false, updatable = false)
    val occurredAt: LocalDateTime,
) : BaseEntity()
