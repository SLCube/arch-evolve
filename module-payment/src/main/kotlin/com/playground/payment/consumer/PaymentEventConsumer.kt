package com.playground.payment.consumer

import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PaymentEventConsumer(
    private val paymentUseCase: PaymentUseCase,
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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