package com.playground.order.application.port.outbound

import com.playground.common.event.DomainEvent


fun interface OrderEventPort {
    fun publish(event: DomainEvent)
}
