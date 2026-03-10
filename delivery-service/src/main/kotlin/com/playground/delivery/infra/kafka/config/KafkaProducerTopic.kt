package com.playground.delivery.infra.kafka.config

import com.playground.delivery.domain.outbox.OutboxEventType

object KafkaProducerTopic {
    const val DELIVERY_CREATED = "delivery.created"

    fun from(eventType: OutboxEventType): String =
        when (eventType) {
            OutboxEventType.DELIVERY_CREATED -> DELIVERY_CREATED
        }
}
