package com.playground.order.application.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.order.contract.domain.event.OrderCompletedEvent
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.domain.outbox.OrderEventOutbox
import com.playground.order.domain.outbox.OutboxEventType
import com.playground.order.domain.outbox.OutboxStatus
import io.micrometer.tracing.Tracer
import org.springframework.stereotype.Component

@Component
class OutboxFactory(
    private val objectMapper: ObjectMapper,
    private val tracer: Tracer,
) {
    private fun currentTraceparent(): String? =
        tracer.currentSpan()?.context()?.let { "00-${it.traceId().lowercase()}-${it.spanId().lowercase()}-01" }

    fun from(event: OrderCreatedEvent): OrderEventOutbox =
        OrderEventOutbox(
            eventId = event.eventId,
            orderId = event.orderId,
            eventType = OutboxEventType.ORDER_CREATED,
            payload = objectMapper.writeValueAsString(event),
            status = OutboxStatus.PENDING,
            occurredAt = event.occurredAt,
            traceparent = currentTraceparent(),
        )

    fun from(event: OrderCompletedEvent): OrderEventOutbox =
        OrderEventOutbox(
            eventId = event.eventId,
            orderId = event.orderId,
            eventType = OutboxEventType.ORDER_COMPLETED,
            payload = objectMapper.writeValueAsString(event),
            status = OutboxStatus.PENDING,
            occurredAt = event.occurredAt,
            traceparent = currentTraceparent(),
        )
}
