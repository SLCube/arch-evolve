package com.playground.delivery.application.support

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.delivery.domain.model.Delivery
import com.playground.delivery.domain.outbox.DeliveryEventOutbox
import com.playground.delivery.domain.outbox.OutboxEventType
import com.playground.delivery.domain.outbox.OutboxStatus
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class OutboxFactory(
    private val objectMapper: ObjectMapper,
) {
    fun deliveryCreated(delivery: Delivery): DeliveryEventOutbox {
        val eventId = UUID.randomUUID()
        val occurredAt = LocalDateTime.now()
        val event =
            mapOf(
                "eventId" to eventId.toString(),
                "deliveryId" to delivery.id,
                "orderId" to delivery.orderId,
                "userId" to delivery.userId,
                "occurredAt" to occurredAt.toString(),
            )
        return DeliveryEventOutbox(
            eventId = eventId,
            orderId = delivery.orderId,
            eventType = OutboxEventType.DELIVERY_CREATED,
            payload = objectMapper.writeValueAsString(event),
            status = OutboxStatus.PENDING,
            occurredAt = occurredAt,
        )
    }
}
