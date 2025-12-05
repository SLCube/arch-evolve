package com.playground.delivery.consumer

import com.playground.delivery.application.port.inbound.DeliveryUsecase
import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand
import com.playground.order.contract.domain.event.OrderCompletedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class DeliveryEventConsumer(
    private val deliveryUsecase: DeliveryUsecase,
) {

    @TransactionalEventListener
    fun handleOrderCompletedEvent(event: OrderCompletedEvent) {
        deliveryUsecase.createDelivery(
            DeliveryCreateCommand(
                foo = "foo"
            )
        )
    }
}