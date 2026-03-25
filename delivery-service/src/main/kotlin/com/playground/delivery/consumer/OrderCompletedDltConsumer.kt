package com.playground.delivery.consumer

import com.playground.delivery.common.log.utils.logger
import com.playground.delivery.consumer.config.KafkaConsumerTopic
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderCompletedDltConsumer(
    meterRegistry: MeterRegistry,
) {
    private val log = logger()

    private val counter =
        Counter
            .builder("dlt.message.count")
            .tag("service", "delivery")
            .tag("topic", KafkaConsumerTopic.ORDER_COMPLETED_DLT)
            .register(meterRegistry)

    @KafkaListener(
        topics = [KafkaConsumerTopic.ORDER_COMPLETED_DLT],
        containerFactory = "dltListenerContainerFactory",
    )
    fun consume(record: ConsumerRecord<String, String>) {
        counter.increment()
        log.error(
            "DLT 메시지 수신 [topic={}, partition={}, offset={}, key={}] payload={}",
            record.topic(),
            record.partition(),
            record.offset(),
            record.key(),
            record.value(),
        )
    }
}
