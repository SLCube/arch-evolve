package com.playground.order.consumer

import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.command.OrderCompleteCommand
import com.playground.order.application.port.inbound.command.OrderFailCommand
import com.playground.payment.contract.domain.event.PaymentCompletedEvent
import com.playground.payment.contract.domain.event.PaymentFailedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderEventConsumer(
    private val orderCommandUseCase: OrderCommandUseCase,
) {
    @TransactionalEventListener
    fun handlePaymentCompletedEvent(event: PaymentCompletedEvent) {
        orderCommandUseCase.completeOrder(
            OrderCompleteCommand(
                orderId = event.orderId,
                pgTransactionId = event.pgTransactionId,
                paidAmount = event.amount,
            )
        )
    }

    @EventListener
    fun handlePaymentFailedEvent(event: PaymentFailedEvent) {
        orderCommandUseCase.failOrder(OrderFailCommand(event.orderId))
    }
}