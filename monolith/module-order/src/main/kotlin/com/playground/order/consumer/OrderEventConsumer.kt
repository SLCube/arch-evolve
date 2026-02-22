package com.playground.order.consumer

import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.command.OrderFailCommand
import com.playground.payment.contract.domain.event.PaymentFailedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class OrderEventConsumer(
    private val orderCommandUseCase: OrderCommandUseCase,
) {
    @Async("eventExecutor")
    @EventListener
    fun handlePaymentFailedEvent(event: PaymentFailedEvent) {
        orderCommandUseCase.failOrder(OrderFailCommand(event.orderId))
    }
}