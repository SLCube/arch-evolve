package com.playground.payment.infra.event

import com.playground.common.event.DomainEvent
import com.playground.payment.application.port.outbound.PaymentEventPort
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class PaymentEventAdapter(
    private val publisher: ApplicationEventPublisher
) : PaymentEventPort {
    override fun publish(event: DomainEvent) {
        publisher.publishEvent(event)
    }
}