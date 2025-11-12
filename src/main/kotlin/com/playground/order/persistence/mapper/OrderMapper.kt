package com.playground.order.persistence.mapper

import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderProduct
import com.playground.order.persistence.entity.OrderJpaEntity
import com.playground.order.persistence.entity.OrderProductJpaEntity

fun OrderJpaEntity.toDomain(orderProducts: MutableList<OrderProduct>): Order {
    return Order(
        id = this.id,
        userId = this.userId,
        totalPrice = this.totalPrice,
        status = this.status,
        orderProducts = orderProducts
    )
}

fun OrderProductJpaEntity.toDomain(): OrderProduct {
    return OrderProduct(
        id = this.id,
        productId = this.productId,
        quantity = this.quantity,
        price = this.price
    )
}