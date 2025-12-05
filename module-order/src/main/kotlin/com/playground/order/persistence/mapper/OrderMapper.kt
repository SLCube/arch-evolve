package com.playground.order.persistence.mapper

import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderAddress
import com.playground.order.domain.model.OrderProduct
import com.playground.order.domain.model.OrderReceiver
import com.playground.order.persistence.entity.OrderAddressEmbedded
import com.playground.order.persistence.entity.OrderJpaEntity
import com.playground.order.persistence.entity.OrderProductJpaEntity
import com.playground.order.persistence.entity.OrderReceiverEmbedded

fun OrderJpaEntity.toDomain(orderProducts: MutableList<OrderProduct>): Order =
    Order(
        id = this.id,
        userId = this.userId,
        totalPrice = this.totalPrice,
        status = this.status,
        orderProducts = orderProducts,
        orderAddress = this.orderAddress.toDomain(),
        orderReceiver = this.orderReceiver.toDomain(),
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
    )

fun OrderProductJpaEntity.toDomain(): OrderProduct =
    OrderProduct(
        id = this.id,
        productId = this.productId,
        quantity = this.quantity,
        price = this.price,
    )

fun OrderAddressEmbedded.toDomain(): OrderAddress {
    return OrderAddress(
        zipCode = this.zipCode,
        baseAddress = this.baseAddress,
        detailAddress = this.detailAddress,
    )
}

fun OrderReceiverEmbedded.toDomain(): OrderReceiver {
    return OrderReceiver(
        receiverName = this.receiverName,
        reveiverPhoneNumber = this.receiverPhoneNumber,
    )
}