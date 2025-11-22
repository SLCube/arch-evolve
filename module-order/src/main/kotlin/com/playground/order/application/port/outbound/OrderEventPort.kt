package com.playground.order.application.port.outbound

import com.playground.order.contract.domain.event.OrderCreatedEvent


fun interface OrderEventPort {
    fun publish(event: OrderCreatedEvent)
}
