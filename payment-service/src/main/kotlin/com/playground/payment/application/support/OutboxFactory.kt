package com.playground.payment.application.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.payment.domain.event.PaymentAuthorizedEvent
import com.playground.payment.domain.event.PaymentDomainEvent
import com.playground.payment.domain.event.PaymentFailedEvent
import com.playground.payment.domain.outbox.OutboxEventType
import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.domain.outbox.PaymentEventOutbox
import io.micrometer.tracing.Tracer
import org.springframework.stereotype.Component

@Component
class OutboxFactory(
    private val objectMapper: ObjectMapper,
    private val tracer: Tracer,
) {
    fun from(event: PaymentDomainEvent): PaymentEventOutbox {
        val eventType =
            when (event) {
                is PaymentAuthorizedEvent -> OutboxEventType.PAYMENT_AUTHORIZED
                is PaymentFailedEvent -> OutboxEventType.PAYMENT_FAILED
            }
        val traceparent = tracer.currentSpan()?.context()?.let { "00-${it.traceId().lowercase()}-${it.spanId().lowercase()}-01" }
        return PaymentEventOutbox(
            eventId = event.eventId,
            orderId = event.orderId,
            eventType = eventType,
            payload = objectMapper.writeValueAsString(event),
            status = OutboxStatus.PENDING,
            occurredAt = event.occurredAt,
            traceparent = traceparent,
        )
    }
}
