package com.playground.order.infra.kafka.producer

import com.playground.order.application.port.outbound.OrderEventPublisherPort
import com.playground.order.domain.outbox.OrderEventOutbox
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import com.playground.common.kafka.config.KafkaProducerTopic
import com.playground.common.log.mdc.HeaderKeys

@Component
class KafkaOrderEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) : OrderEventPublisherPort {
    override fun publish(outbox: OrderEventOutbox) {
        val record =
            ProducerRecord<String, String>(
                KafkaProducerTopic.ORDER_CREATED,
                outbox.eventId.toString(),
                outbox.payload,
            )
        record.headers().add(HeaderKeys.X_REQUEST_ID, outbox.requestId.toByteArray())
        kafkaTemplate.send(record).get()
    }
}
