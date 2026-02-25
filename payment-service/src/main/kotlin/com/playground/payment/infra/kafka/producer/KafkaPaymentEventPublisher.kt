package com.playground.payment.infra.kafka.producer

import com.playground.payment.application.port.outbound.PaymentEventPublisherPort
import com.playground.payment.domain.outbox.PaymentEventOutbox
import com.playground.payment.infra.kafka.config.KafkaTopic
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaPaymentEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) : PaymentEventPublisherPort {
    override fun publish(outbox: PaymentEventOutbox) {
        kafkaTemplate.send(KafkaTopic.from(outbox.eventType), outbox.eventId.toString(), outbox.payload).get()
    }
}
