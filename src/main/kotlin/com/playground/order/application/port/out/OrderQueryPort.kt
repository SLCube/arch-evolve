package com.playground.order.application.port.out

import com.playground.order.domain.model.Order

interface OrderQueryPort {
    fun findById(orderId: Long): Order
}