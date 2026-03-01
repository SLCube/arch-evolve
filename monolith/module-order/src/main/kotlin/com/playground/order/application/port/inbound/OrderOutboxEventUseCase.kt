package com.playground.order.application.port.inbound

import com.playground.order.domain.outbox.OrderEventOutbox

interface OrderOutboxEventUseCase {
    fun findPendingEvents(): List<OrderEventOutbox>

    fun publishEvent(outbox: OrderEventOutbox)
}
