package com.playground.order.application.factory

import com.playground.order.contract.domain.event.OrderCompletedEvent
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.contract.domain.event.OrderFailedEvent
import com.playground.order.domain.model.Order
import java.time.LocalDateTime
import java.util.UUID

fun OrderCreatedEvent.Companion.from(order: Order): OrderCreatedEvent {
    val orderProductDetails =
        order.orderProducts.map {
            OrderCreatedEvent.OrderProductDetail(
                productId = it.productId,
                quantity = it.quantity,
            )
        }
    return OrderCreatedEvent(
        eventId = UUID.randomUUID(),
        orderId = order.id!!,
        userId = order.userId,
        products = orderProductDetails,
        totalAmount = order.totalPrice,
        occurredAt = LocalDateTime.now(),
    )
}

fun OrderCompletedEvent.Companion.from(order: Order): OrderCompletedEvent {
    val orderProductDetails =
        order.orderProducts.map {
            OrderCompletedEvent.OrderProductDetail(
                productId = it.productId,
                quantity = it.quantity,
            )
        }
    return OrderCompletedEvent(
        userId = order.userId,
        orderId = order.id!!,
        products = orderProductDetails,
        totalAmount = order.totalPrice,
        receiverName = order.orderReceiver.receiverName,
        receiverPhoneNumber = order.orderReceiver.receiverPhoneNumber,
        zipCode = order.orderAddress.zipCode,
        baseAddress = order.orderAddress.baseAddress,
        detailAddress = order.orderAddress.detailAddress,
    )
}

fun OrderFailedEvent.Companion.from(order: Order): OrderFailedEvent {
    val orderProductDetails =
        order.orderProducts.map {
            OrderFailedEvent.OrderProductDetail(
                productId = it.productId,
                quantity = it.quantity,
            )
        }
    return OrderFailedEvent(
        orderId = order.id!!,
        userId = order.userId,
        products = orderProductDetails,
        totalAmount = order.totalPrice,
    )
}
