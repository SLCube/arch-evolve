package com.playground.order.infra.kafka.producer

import com.playground.common.kafka.config.KafkaProducerTopic
import com.playground.common.log.mdc.HeaderKeys
import com.playground.order.application.port.outbound.OrderEventPublisherPort
import com.playground.order.domain.outbox.OrderEventOutbox
import org.apache.kafka.clients.producer.ProducerRecord
import com.playground.common.log.utils.logger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class KafkaOrderEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) : OrderEventPublisherPort {
    private val log = logger()

    override fun publishAll(outboxes: List<OrderEventOutbox>): List<OrderEventOutbox> {
        val futures =
            outboxes.map { outbox ->
                val record =
                    ProducerRecord<String, String>(
                        KafkaProducerTopic.ORDER_CREATED,
                        outbox.eventId.toString(),
                        outbox.payload,
                    )
                record.headers().add(HeaderKeys.X_REQUEST_ID, outbox.requestId.toByteArray())
                outbox to kafkaTemplate.send(record)
            }

        kafkaTemplate.flush()

        return futures.mapNotNull { (outbox, future) ->
            try {
                future.get()
                outbox
            } catch (e: Exception) {
                log.error("Kafka 발행 실패 [eventId={}]", outbox.eventId, e)
                null
            }
        }
    }
}
