package com.playground.delivery.consumer

import com.playground.common.log.utils.logger
import com.playground.delivery.contract.domain.event.DeliveryCompletedEvent
import com.playground.delivery.contract.domain.event.DeliveryCreatedEvent
import com.playground.delivery.contract.domain.event.DeliveryStartedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class DeliveryLoggingEventConsumer {
    private val log = logger()

    @TransactionalEventListener
    fun handleDeliveryCreatedEvent(event: DeliveryCreatedEvent) {
        log.info(
            "배송 생성 완료. deliveryId={}, orderId={}, userId={}",
            event.deliveryId,
            event.orderId,
            event.userId,
        )
    }

    @TransactionalEventListener
    fun handleDeliveryStartedEvent(event: DeliveryStartedEvent) {
        log.info(
            "배송 시작. deliveryId={}, orderId={}, userId={}, shippedAt={}",
            event.deliveryId,
            event.orderId,
            event.userId,
            event.shippedAt,
        )
    }

    @TransactionalEventListener
    fun handleDeliveryCompletedEvent(event: DeliveryCompletedEvent) {
        log.info(
            "배송 완료. deliveryId={}, orderId={}, userId={}, deliveredAt={}",
            event.deliveryId,
            event.orderId,
            event.userId,
            event.deliveredAt,
        )
    }
}
