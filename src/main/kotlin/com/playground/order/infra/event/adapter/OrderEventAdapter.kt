package com.playground.order.infra.event.adapter

import com.playground.order.application.port.out.OrderEventPort
import com.playground.order.domain.event.OrderCreatedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class OrderEventAdapter(
    private val publisher: ApplicationEventPublisher
): OrderEventPort {
    override fun publish(event: OrderCreatedEvent) {
        publisher.publishEvent(event)
    }
}