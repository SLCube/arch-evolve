package com.playground.order.consumer

import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.command.OrderCompleteCommand
import com.playground.payment.contract.domain.event.PaymentCompletedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderEventConsumer(
    private val orderCommandUseCase: OrderCommandUseCase,
) {
    @TransactionalEventListener
    fun handlePaymentCompletedEvent(event: PaymentCompletedEvent) {
        val command = OrderCompleteCommand(
            orderId = event.orderId,
            pgTransactionId = event.pgTransactionId,
            paidAmount = event.amount,
        )

        orderCommandUseCase.completeOrder(command)
    }
}