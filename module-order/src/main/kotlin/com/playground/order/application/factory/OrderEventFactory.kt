package com.playground.order.application.factory

import com.playground.order.contract.domain.event.OrderCompletedEvent
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.domain.model.Order

fun OrderCreatedEvent.Companion.from(order: Order): OrderCreatedEvent {
    val orderProductDetails =
        order.orderProducts.map {
            OrderCreatedEvent.OrderProductDetail(
                productId = it.productId,
                quantity = it.quantity,
            )
        }
    return OrderCreatedEvent(
        orderId = order.id!!,
        userId = order.userId,
        products = orderProductDetails,
        totalAmount = order.totalPrice,
    )
}

fun OrderCompletedEvent.Companion.from(order: Order): OrderCompletedEvent {
    return OrderCompletedEvent(
        userId = order.userId,
        orderId = order.id!!,
        totalAmount = order.totalPrice,
        receiverName = order.orderReceiver.receiverName,
        receiverPhoneNumber = order.orderReceiver.receiverPhoneNumber,
        zipCode = order.orderAddress.zipCode,
        baseAddress = order.orderAddress.baseAddress,
        detailAddress = order.orderAddress.detailAddress,
    )
}
