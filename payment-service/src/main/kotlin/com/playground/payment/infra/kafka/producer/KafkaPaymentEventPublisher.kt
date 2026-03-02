package com.playground.payment.infra.kafka.producer

import com.playground.payment.application.port.outbound.PaymentEventPublisherPort
import com.playground.payment.common.log.mdc.HeaderKeys
import com.playground.payment.domain.outbox.PaymentEventOutbox
import com.playground.payment.infra.kafka.config.KafkaProducerTopic
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaPaymentEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) : PaymentEventPublisherPort {
    override fun publish(outbox: PaymentEventOutbox) {
        val record =
            ProducerRecord<String, String>(
                KafkaProducerTopic.from(outbox.eventType),
                outbox.eventId.toString(),
                outbox.payload,
            )
        record.headers().add(HeaderKeys.X_REQUEST_ID, outbox.requestId.toByteArray())
        kafkaTemplate.send(record).get()
    }
}
