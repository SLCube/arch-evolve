package com.playground.order.infra.event.adapter

import com.playground.common.event.DomainEvent
import com.playground.order.application.port.outbound.OrderEventPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class OrderEventAdapter(
    private val publisher: ApplicationEventPublisher,
) : OrderEventPort {
    override fun publish(event: DomainEvent) {
        publisher.publishEvent(event)
    }
}
