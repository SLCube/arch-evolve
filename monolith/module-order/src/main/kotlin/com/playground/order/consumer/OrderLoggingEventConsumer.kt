package com.playground.order.consumer

import com.playground.common.log.utils.logger
import com.playground.order.contract.domain.event.OrderCompletedEvent
import com.playground.order.contract.domain.event.OrderCreatedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderLoggingEventConsumer {
    private val log = logger()

    @TransactionalEventListener
    fun handleOrderCreatedEvent(event: OrderCreatedEvent) {
        log.info(
            "New order created. orderId={}, userId={}, productsCount={}",
            event.orderId,
            event.userId,
            event.products.size,
        )
    }

    @TransactionalEventListener
    fun handleOrderCompletedEvent(event: OrderCompletedEvent) {
        log.info(
            "Order completed. orderId={}, userId={}, totalAmount={}, receiverName={}, zipCode={}",
            event.orderId,
            event.userId,
            event.totalAmount,
            event.receiverName,
            event.zipCode,
        )
    }
}
