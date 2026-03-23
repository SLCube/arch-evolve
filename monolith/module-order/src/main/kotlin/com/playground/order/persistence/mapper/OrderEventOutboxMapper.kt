package com.playground.order.persistence.mapper

import com.playground.order.domain.outbox.OrderEventOutbox
import com.playground.order.persistence.entity.OrderEventOutboxJpaEntity

fun OrderEventOutboxJpaEntity.toDomain(): OrderEventOutbox =
    OrderEventOutbox(
        id = this.id,
        eventId = this.eventId,
        orderId = this.orderId,
        eventType = this.eventType,
        payload = this.payload,
        status = this.status,
        occurredAt = this.occurredAt,
        traceparent = this.traceparent,
        retryCount = this.retryCount,
    )

fun OrderEventOutbox.toJpaEntity(): OrderEventOutboxJpaEntity =
    OrderEventOutboxJpaEntity(
        id = this.id,
        eventId = this.eventId,
        orderId = this.orderId,
        eventType = this.eventType,
        payload = this.payload,
        status = this.status,
        occurredAt = this.occurredAt,
        traceparent = this.traceparent,
        retryCount = this.retryCount,
    )
