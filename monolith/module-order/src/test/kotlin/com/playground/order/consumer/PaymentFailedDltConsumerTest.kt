package com.playground.order.consumer

import io.kotest.matchers.shouldBe
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class PaymentFailedDltConsumerTest {
    private val meterRegistry = SimpleMeterRegistry()
    private val consumer = PaymentFailedDltConsumer(meterRegistry)

    @Test
    fun `DLT 메시지를 수신하면 카운터가 증가해야 한다`() {
        val record = ConsumerRecord<String, String>("payment-failed.DLT", 0, 0L, "1", "{}")

        consumer.consume(record)

        meterRegistry
            .counter("dlt.message.count", "service", "order", "topic", "payment-failed.DLT")
            .count() shouldBe 1.0
    }

    @Test
    fun `DLT 메시지 처리 시 예외를 던지지 않아야 한다`() {
        val record = ConsumerRecord<String, String>("payment-failed.DLT", 0, 0L, "1", "invalid-json")

        consumer.consume(record)
    }
}
