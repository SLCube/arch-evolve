package com.playground.delivery.infra.event.adapter

import com.playground.common.event.DomainEvent
import com.playground.delivery.application.port.outbound.DeliveryEventPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class DeliveryEventAdapter(
    private val publisher: ApplicationEventPublisher,
) : DeliveryEventPort {
    override fun publish(event: DomainEvent) {
        publisher.publishEvent(event)
    }
}

