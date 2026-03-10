package com.playground.delivery.persistence.mapper

import com.playground.delivery.domain.outbox.DeliveryEventOutbox
import com.playground.delivery.persistence.entity.DeliveryEventOutboxJpaEntity

fun DeliveryEventOutboxJpaEntity.toDomain(): DeliveryEventOutbox =
    DeliveryEventOutbox(
        id = this.id,
        eventId = this.eventId,
        orderId = this.orderId,
        eventType = this.eventType,
        payload = this.payload,
        status = this.status,
        occurredAt = this.occurredAt,
    )

fun DeliveryEventOutbox.toJpaEntity(): DeliveryEventOutboxJpaEntity =
    DeliveryEventOutboxJpaEntity(
        id = this.id,
        eventId = this.eventId,
        orderId = this.orderId,
        eventType = this.eventType,
        payload = this.payload,
        status = this.status,
        occurredAt = this.occurredAt,
    )
