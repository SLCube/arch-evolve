package com.playground.order.application.port.outbound

import com.playground.order.domain.outbox.OrderEventOutbox

fun interface OrderEventPublisherPort {
    fun publish(outbox: OrderEventOutbox)
}
