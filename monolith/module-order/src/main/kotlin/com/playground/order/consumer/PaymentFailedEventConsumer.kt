package com.playground.order.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.command.OrderFailCommand
import com.playground.order.consumer.config.KafkaConsumerTopic
import com.playground.order.consumer.event.PaymentFailedEvent
import com.playground.order.consumer.support.KafkaConsumerHandler
import com.playground.common.log.utils.logger
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class PaymentFailedEventConsumer(
    private val orderCommandUseCase: OrderCommandUseCase,
    private val objectMapper: ObjectMapper,
) {
    private val log = logger()

    @KafkaConsumerHandler
    @KafkaListener(topics = [KafkaConsumerTopic.PAYMENT_FAILED])
    fun consume(
        @Payload payload: String,
    ) {
        val event = objectMapper.readValue(payload, PaymentFailedEvent::class.java)
        log.info("결제 실패 이벤트 수신 [orderId={}]", event.orderId)

        orderCommandUseCase.failOrder(OrderFailCommand(event.orderId))
    }
}
