package com.playground.order.application.port.out

import com.playground.order.domain.model.Order

interface OrderCommandPort {
    fun save(order: Order): Order
}