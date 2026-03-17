package com.playground.delivery.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.delivery.application.port.inbound.DeliveryUseCase
import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand
import com.playground.delivery.common.log.utils.logger
import com.playground.delivery.consumer.config.KafkaConsumerTopic
import com.playground.delivery.consumer.event.OrderCompletedEvent
import com.playground.delivery.consumer.support.KafkaConsumerHandler
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderCompletedEventConsumer(
    private val deliveryUseCase: DeliveryUseCase,
    private val objectMapper: ObjectMapper,
) {
    private val log = logger()

    @KafkaConsumerHandler
    @KafkaListener(topics = [KafkaConsumerTopic.ORDER_COMPLETED], containerFactory = "orderCompletedListenerContainerFactory")
    fun consume(record: ConsumerRecord<String, String>) {
        val event = objectMapper.readValue(record.value(), OrderCompletedEvent::class.java)
        log.info("주문 완료 이벤트 수신 [orderId={}, userId={}]", event.orderId, event.userId)
        deliveryUseCase.createDelivery(
            DeliveryCreateCommand(
                orderId = event.orderId,
                userId = event.userId,
                receiverName = event.receiverName,
                receiverPhoneNumber = event.receiverPhoneNumber,
                zipCode = event.zipCode,
                baseAddress = event.baseAddress,
                detailAddress = event.detailAddress,
            ),
        )
    }
}
