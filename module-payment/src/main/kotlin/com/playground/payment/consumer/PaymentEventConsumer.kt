package com.playground.payment.consumer

import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.application.port.inbound.command.AuthorizePaymentCommand
import org.springframework.stereotype.Component

@Component
class PaymentEventConsumer(
    private val paymentUseCase: PaymentUseCase,
) {

    fun handleOrderCreatedEvent(event: OrderCreatedEvent) {
        paymentUseCase.authorizePayment(
            AuthorizePaymentCommand(
                userId = event.userId,
                orderId = event.orderId,
                amount = event.totalAmount
            )
        )
    }
}