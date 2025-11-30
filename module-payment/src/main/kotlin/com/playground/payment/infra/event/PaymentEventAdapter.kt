package com.playground.payment.infra.event

import com.playground.payment.application.port.outbound.PaymentEventPort
import com.playground.payment.contract.domain.event.PaymentCompletedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class PaymentEventAdapter(
    private val publisher: ApplicationEventPublisher
) : PaymentEventPort {
    override fun publish(event: PaymentCompletedEvent) {
        publisher.publishEvent(event)
    }
}