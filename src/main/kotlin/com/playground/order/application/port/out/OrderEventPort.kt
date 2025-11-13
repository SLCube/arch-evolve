package com.playground.order.application.port.out

import com.playground.order.domain.event.OrderCreatedEvent

interface OrderEventPort {
    fun publish(event: OrderCreatedEvent)
}