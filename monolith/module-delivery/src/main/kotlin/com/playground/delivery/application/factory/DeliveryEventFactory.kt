package com.playground.delivery.application.factory

import com.playground.delivery.contract.domain.event.DeliveryCompletedEvent
import com.playground.delivery.contract.domain.event.DeliveryCreatedEvent
import com.playground.delivery.contract.domain.event.DeliveryStartedEvent
import com.playground.delivery.domain.model.Delivery

fun DeliveryCreatedEvent.Companion.from(delivery: Delivery): DeliveryCreatedEvent {
    return DeliveryCreatedEvent(
        deliveryId = requireNotNull(delivery.id),
        orderId = delivery.orderId,
        userId = delivery.userId,
    )
}

fun DeliveryStartedEvent.Companion.from(delivery: Delivery): DeliveryStartedEvent {
    return DeliveryStartedEvent(
        deliveryId = requireNotNull(delivery.id),
        orderId = delivery.orderId,
        userId = delivery.userId,
        shippedAt = requireNotNull(delivery.shippedAt),
    )
}

fun DeliveryCompletedEvent.Companion.from(delivery: Delivery): DeliveryCompletedEvent {
    return DeliveryCompletedEvent(
        deliveryId = requireNotNull(delivery.id),
        orderId = delivery.orderId,
        userId = delivery.userId,
        deliveredAt = requireNotNull(delivery.deliveredAt),
    )
}

