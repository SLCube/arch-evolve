package com.playground.payment.consumer

import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
class PaymentEventConsumer(
    private val paymentUseCase: PaymentUseCase,
) {

    @Order(2)
    @EventListener
    fun handleOrderCreatedEvent(event: OrderCreatedEvent) {
        paymentUseCase.authorizePayment(
            PaymentAuthorizeCommand(
                userId = event.userId,
                orderId = event.orderId,
                amount = event.totalAmount
            )
        )
    }
}