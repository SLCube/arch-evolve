package com.playground.order.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.command.OrderFailCommand
import com.playground.order.consumer.config.KafkaConsumerTopic
import com.playground.order.consumer.event.PaymentFailedEvent
import com.playground.order.consumer.support.KafkaConsumerHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class PaymentFailedEventConsumer(
    private val orderCommandUseCase: OrderCommandUseCase,
    private val objectMapper: ObjectMapper,
) {
    @KafkaConsumerHandler
    @KafkaListener(topics = [KafkaConsumerTopic.PAYMENT_FAILED])
    fun consume(
        @Payload payload: String,
    ) {
        val event = objectMapper.readValue(payload, PaymentFailedEvent::class.java)

        orderCommandUseCase.failOrder(OrderFailCommand(event.orderId))
    }
}
