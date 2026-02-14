package com.playground.order.application.port.outbound

import com.playground.order.domain.model.Order

interface OrderCommandPort {
    fun save(order: Order): Order

    fun update(order: Order): Order
}
