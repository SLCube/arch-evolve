package com.playground.payment.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import com.playground.payment.common.log.utils.logger
import com.playground.payment.consumer.config.KafkaConsumerTopic
import com.playground.payment.consumer.event.OrderCreatedEvent
import com.playground.payment.consumer.support.KafkaConsumerHandler
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderCreatedEventConsumer(
    private val paymentUseCase: PaymentUseCase,
    private val objectMapper: ObjectMapper,
) {
    private val log = logger()

    @KafkaConsumerHandler
    @KafkaListener(topics = [KafkaConsumerTopic.ORDER_CREATED], containerFactory = "orderCreatedListenerContainerFactory")
    fun consume(record: ConsumerRecord<String, String>) {
        val event = objectMapper.readValue(record.value(), OrderCreatedEvent::class.java)
        log.info("주문 생성 이벤트 수신 [orderId={}, userId={}]", event.orderId, event.userId)
        paymentUseCase.authorizePayment(
            PaymentAuthorizeCommand(
                userId = event.userId,
                orderId = event.orderId,
                amount = event.totalAmount,
            ),
        )
    }
}
