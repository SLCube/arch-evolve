package com.playground.order.application.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.domain.outbox.OrderEventOutbox
import com.playground.order.domain.outbox.OutboxEventType
import com.playground.order.domain.outbox.OutboxStatus
import org.slf4j.MDC
import org.springframework.stereotype.Component

@Component
class OutboxFactory(
    private val objectMapper: ObjectMapper,
) {
    fun from(event: OrderCreatedEvent): OrderEventOutbox =
        OrderEventOutbox(
            eventId = event.eventId,
            eventType = OutboxEventType.ORDER_CREATED,
            payload = objectMapper.writeValueAsString(event),
            status = OutboxStatus.PENDING,
            occurredAt = event.occurredAt,
            requestId = MDC.get("requestId") ?: "",
        )
}
