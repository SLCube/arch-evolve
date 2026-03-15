package com.playground.order.infra.kafka.producer

import com.playground.common.kafka.config.KafkaProducerTopic
import com.playground.order.application.port.outbound.OrderEventPublisherPort
import com.playground.order.domain.outbox.OrderEventOutbox
import com.playground.order.domain.outbox.OutboxEventType
import org.apache.kafka.clients.producer.ProducerRecord
import com.playground.common.log.utils.logger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaOrderEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) : OrderEventPublisherPort {
    private val log = logger()

    override fun publishAll(outboxes: List<OrderEventOutbox>): List<OrderEventOutbox> =
        kafkaTemplate.sendAll(
            items = outboxes,
            toRecord = { outbox ->
                ProducerRecord<String, String>(
                    topicFrom(outbox.eventType),
                    outbox.orderId.toString(),
                    outbox.payload,
                ).also { record ->
                    outbox.traceparent?.let { record.headers().add("traceparent", it.toByteArray()) }
                }
            },
            onError = { outbox, e -> log.error("Kafka 발행 실패 [eventId={}]", outbox.eventId, e) },
        )

    private fun topicFrom(eventType: OutboxEventType): String =
        when (eventType) {
            OutboxEventType.ORDER_CREATED -> KafkaProducerTopic.ORDER_CREATED
            OutboxEventType.ORDER_COMPLETED -> KafkaProducerTopic.ORDER_COMPLETED
        }
}
