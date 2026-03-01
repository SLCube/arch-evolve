package com.playground.order.infra.kafka.producer

import com.playground.order.application.port.outbound.OrderEventPublisherPort
import com.playground.order.domain.outbox.OrderEventOutbox
import playground.common.kafka.config.KafkaTopic
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaOrderEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) : OrderEventPublisherPort {
    override fun publish(outbox: OrderEventOutbox) {
        val record =
            ProducerRecord<String, String>(
                KafkaTopic.ORDER_CREATED,
                outbox.eventId.toString(),
                outbox.payload,
            )
        record.headers().add("X-Request-Id", outbox.requestId.toByteArray())
        kafkaTemplate.send(record).get()
    }
}
