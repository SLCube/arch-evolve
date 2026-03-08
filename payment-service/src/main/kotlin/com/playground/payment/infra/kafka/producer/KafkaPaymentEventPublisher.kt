package com.playground.payment.infra.kafka.producer

import com.playground.payment.application.port.outbound.PaymentEventPublisherPort
import com.playground.payment.common.log.mdc.HeaderKeys
import com.playground.payment.common.log.utils.logger
import com.playground.payment.domain.outbox.PaymentEventOutbox
import com.playground.payment.infra.kafka.config.KafkaProducerTopic
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaPaymentEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) : PaymentEventPublisherPort {
    private val log = logger()

    override fun publishAll(outboxes: List<PaymentEventOutbox>): List<PaymentEventOutbox> =
        kafkaTemplate.sendAll(
            items = outboxes,
            toRecord = { outbox ->
                ProducerRecord<String, String>(
                    KafkaProducerTopic.from(outbox.eventType),
                    outbox.eventId.toString(),
                    outbox.payload,
                ).apply {
                    headers().add(HeaderKeys.X_REQUEST_ID, outbox.requestId.toByteArray())
                }
            },
            onError = { outbox, e -> log.error("Kafka 발행 실패 [eventId={}]", outbox.eventId, e) },
        )
}
