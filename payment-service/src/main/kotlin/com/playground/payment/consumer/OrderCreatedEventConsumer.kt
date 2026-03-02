package com.playground.payment.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import com.playground.payment.consumer.config.KafkaConsumerTopic
import com.playground.payment.consumer.event.OrderCreatedEvent
import com.playground.payment.consumer.support.KafkaConsumerHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class OrderCreatedEventConsumer(
    private val paymentUseCase: PaymentUseCase,
    private val objectMapper: ObjectMapper,
) {
    @KafkaConsumerHandler
    @KafkaListener(topics = [KafkaConsumerTopic.ORDER_CREATED])
    fun consume(
        @Payload payload: String,
    ) {
        val event = objectMapper.readValue(payload, OrderCreatedEvent::class.java)

        val command =
            PaymentAuthorizeCommand(
                userId = event.userId,
                orderId = event.orderId,
                amount = event.totalAmount,
            )

        paymentUseCase.authorizePayment(command)
    }
}
