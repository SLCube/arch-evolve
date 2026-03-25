package com.playground.order.consumer

import com.playground.common.log.utils.logger
import com.playground.order.consumer.config.KafkaConsumerTopic
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PaymentAuthorizedDltConsumer(
    meterRegistry: MeterRegistry,
) {
    private val log = logger()

    private val counter =
        Counter
            .builder("dlt.message.count")
            .tag("service", "order")
            .tag("topic", KafkaConsumerTopic.PAYMENT_AUTHORIZED_DLT)
            .register(meterRegistry)

    @KafkaListener(
        topics = [KafkaConsumerTopic.PAYMENT_AUTHORIZED_DLT],
        containerFactory = "dltListenerContainerFactory",
    )
    fun consume(record: ConsumerRecord<String, String>) {
        counter.increment()
        log.error(
            "DLT 메시지 수신 [topic={}, partition={}, offset={}, key={}] payload={}",
            record.topic(), record.partition(), record.offset(), record.key(), record.value(),
        )
    }
}
