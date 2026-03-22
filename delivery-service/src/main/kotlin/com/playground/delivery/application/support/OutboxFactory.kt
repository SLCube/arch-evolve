package com.playground.delivery.application.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.delivery.domain.event.DeliveryCreatedEvent
import com.playground.delivery.domain.model.Delivery
import com.playground.delivery.domain.outbox.DeliveryEventOutbox
import com.playground.delivery.domain.outbox.OutboxEventType
import com.playground.delivery.domain.outbox.OutboxStatus
import io.micrometer.tracing.Tracer
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class OutboxFactory(
    private val objectMapper: ObjectMapper,
    private val tracer: Tracer,
) {
    fun deliveryCreated(delivery: Delivery): DeliveryEventOutbox {
        val eventId = UUID.randomUUID()
        val occurredAt = LocalDateTime.now()
        val traceparent = tracer.currentSpan()?.context()?.let { "00-${it.traceId().lowercase()}-${it.spanId().lowercase()}-01" }
        val event =
            DeliveryCreatedEvent(
                eventId = eventId,
                deliveryId = delivery.id!!,
                orderId = delivery.orderId,
                userId = delivery.userId,
                occurredAt = occurredAt,
            )
        return DeliveryEventOutbox(
            eventId = eventId,
            orderId = delivery.orderId,
            eventType = OutboxEventType.DELIVERY_CREATED,
            payload = objectMapper.writeValueAsString(event),
            status = OutboxStatus.PENDING,
            occurredAt = occurredAt,
            traceparent = traceparent,
        )
    }
}
