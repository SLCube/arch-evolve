package com.playground.delivery.infra.kafka.producer

import com.playground.delivery.application.port.outbound.DeliveryEventPublisherPort
import com.playground.delivery.common.log.utils.logger
import com.playground.delivery.domain.outbox.DeliveryEventOutbox
import com.playground.delivery.infra.kafka.config.KafkaProducerTopic
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaDeliveryEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) : DeliveryEventPublisherPort {
    private val log = logger()

    override fun publishAll(outboxes: List<DeliveryEventOutbox>): List<DeliveryEventOutbox> =
        kafkaTemplate.sendAll(
            items = outboxes,
            toRecord = { outbox ->
                ProducerRecord<String, String>(
                    KafkaProducerTopic.from(outbox.eventType),
                    outbox.orderId.toString(),
                    outbox.payload,
                )
            },
            onError = { outbox, e -> log.error("Kafka 발행 실패 [eventId={}]", outbox.eventId, e) },
        )
}
