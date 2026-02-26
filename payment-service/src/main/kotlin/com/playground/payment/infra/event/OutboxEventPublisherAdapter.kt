package com.playground.payment.infra.event

import com.playground.payment.application.port.outbound.OutboxEventPublisherPort
import com.playground.payment.domain.outbox.PaymentEventOutbox
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class OutboxEventPublisherAdapter(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : OutboxEventPublisherPort {
    override fun publish(outbox: PaymentEventOutbox) = applicationEventPublisher.publishEvent(outbox)
}
