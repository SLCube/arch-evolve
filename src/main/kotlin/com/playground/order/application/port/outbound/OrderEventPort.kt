package com.playground.order.application.port.outbound

import com.playground.order.domain.event.OrderCreatedEvent

fun interface OrderEventPort {
    fun publish(event: OrderCreatedEvent)
}
