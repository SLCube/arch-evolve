package com.playground.payment.application.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.payment.domain.event.PaymentAuthorizedEvent
import com.playground.payment.domain.event.PaymentDomainEvent
import com.playground.payment.domain.event.PaymentFailedEvent
import com.playground.payment.domain.outbox.OutboxEventType
import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.domain.outbox.PaymentEventOutbox
import org.slf4j.MDC
import org.springframework.stereotype.Component

@Component
class OutboxFactory(
    private val objectMapper: ObjectMapper,
) {
    fun from(event: PaymentDomainEvent): PaymentEventOutbox {
        val eventType =
            when (event) {
                is PaymentAuthorizedEvent -> OutboxEventType.PAYMENT_AUTHORIZED
                is PaymentFailedEvent -> OutboxEventType.PAYMENT_FAILED
            }
        return PaymentEventOutbox(
            eventId = event.eventId,
            eventType = eventType,
            payload = objectMapper.writeValueAsString(event),
            status = OutboxStatus.PENDING,
            occurredAt = event.occurredAt,
            requestId = MDC.get("requestId") ?: "",
        )
    }
}
