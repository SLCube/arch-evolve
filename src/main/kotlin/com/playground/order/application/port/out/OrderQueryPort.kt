package com.playground.order.application.port.out

import com.playground.order.domain.model.Order

fun interface OrderQueryPort {
    fun findById(orderId: Long): Order
}