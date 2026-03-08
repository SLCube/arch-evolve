package com.playground.payment.persistence.mapper

import com.playground.payment.domain.outbox.PaymentEventOutbox
import com.playground.payment.persistence.entity.PaymentEventOutboxJpaEntity

fun PaymentEventOutboxJpaEntity.toDomain(): PaymentEventOutbox =
    PaymentEventOutbox(
        id = this.id,
        eventId = this.eventId,
        eventType = this.eventType,
        payload = this.payload,
        status = this.status,
        occurredAt = this.occurredAt,
    )

fun PaymentEventOutbox.toJpaEntity(): PaymentEventOutboxJpaEntity =
    PaymentEventOutboxJpaEntity(
        id = this.id,
        eventId = this.eventId,
        eventType = this.eventType,
        payload = this.payload,
        status = this.status,
        occurredAt = this.occurredAt,
    )
