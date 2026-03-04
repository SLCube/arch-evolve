package com.playground.order.application.port.outbound

import com.playground.order.domain.outbox.OrderEventOutbox

interface OrderEventPublisherPort {
    fun publishAll(outboxes: List<OrderEventOutbox>): List<OrderEventOutbox>
}
