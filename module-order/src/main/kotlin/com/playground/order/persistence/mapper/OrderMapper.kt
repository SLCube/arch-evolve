package com.playground.order.persistence.mapper

import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderProduct
import com.playground.order.persistence.entity.OrderJpaEntity
import com.playground.order.persistence.entity.OrderProductJpaEntity

fun OrderJpaEntity.toDomain(orderProducts: MutableList<OrderProduct>): Order =
    Order(
        id = this.id,
        userId = this.userId,
        totalPrice = this.totalPrice,
        status = this.status,
        orderProducts = orderProducts,
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
