package com.playground.payment.application.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.payment.domain.event.PaymentAuthorizedEvent
import com.playground.payment.domain.event.PaymentFailedEvent
import com.playground.payment.domain.outbox.OutboxEventType
import com.playground.payment.domain.outbox.OutboxStatus
import com.playground.payment.domain.outbox.PaymentEventOutbox
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class OutboxFactory(
    private val objectMapper: ObjectMapper,
) {
    fun from(event: PaymentAuthorizedEvent): PaymentEventOutbox =
        PaymentEventOutbox(
            eventId = event.eventId,
            eventType = OutboxEventType.PAYMENT_AUTHORIZED,
            payload = objectMapper.writeValueAsString(event),
            status = OutboxStatus.PENDING,
            occurredAt = LocalDateTime.now(),
        )

    fun from(event: PaymentFailedEvent): PaymentEventOutbox =
        PaymentEventOutbox(
            eventId = event.eventId,
            eventType = OutboxEventType.PAYMENT_FAILED,
            payload = objectMapper.writeValueAsString(event),
            status = OutboxStatus.PENDING,
            occurredAt = LocalDateTime.now(),
        )
}
