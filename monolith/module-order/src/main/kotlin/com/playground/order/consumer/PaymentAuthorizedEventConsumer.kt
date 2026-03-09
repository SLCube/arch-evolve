package com.playground.order.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.command.OrderCompleteCommand
import com.playground.order.consumer.config.KafkaConsumerTopic
import com.playground.order.consumer.event.PaymentAuthorizedEvent
import com.playground.order.consumer.support.KafkaConsumerHandler
import com.playground.common.log.utils.logger
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class PaymentAuthorizedEventConsumer(
    private val orderCommandUseCase: OrderCommandUseCase,
    private val objectMapper: ObjectMapper,
) {
    private val log = logger()

    @KafkaConsumerHandler
    @KafkaListener(topics = [KafkaConsumerTopic.PAYMENT_AUTHORIZED])
    fun consume(record: ConsumerRecord<String, String>) {
        val event = objectMapper.readValue(record.value(), PaymentAuthorizedEvent::class.java)
        log.info("결제 승인 이벤트 수신 [orderId={}, pgTxId={}]", event.orderId, event.pgTransactionId)
        orderCommandUseCase.completeOrder(
            OrderCompleteCommand(
                orderId = event.orderId,
                pgTransactionId = event.pgTransactionId,
                paidAmount = event.amount,
            ),
        )
    }
}
