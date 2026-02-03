package com.playground.delivery.consumer

import com.playground.delivery.application.port.inbound.DeliveryUsecase
import com.playground.delivery.application.port.inbound.command.DeliveryCreateCommand
import com.playground.order.contract.domain.event.OrderCompletedEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class DeliveryEventConsumer(
    private val deliveryUsecase: DeliveryUsecase,
) {

    @Async("eventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleOrderCompletedEvent(event: OrderCompletedEvent) {
        deliveryUsecase.createDelivery(
            DeliveryCreateCommand(
                orderId = event.orderId,
                userId = event.userId,
                receiverName = event.receiverName,
                receiverPhoneNumber = event.receiverPhoneNumber,
                zipCode = event.zipCode,
                baseAddress = event.baseAddress,
                detailAddress = event.detailAddress,
            )
        )
    }
}
