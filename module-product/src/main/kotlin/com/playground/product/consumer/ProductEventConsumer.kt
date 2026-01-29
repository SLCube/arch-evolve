package com.playground.product.consumer

import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.product.application.port.inbound.StockUseCase
import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
class ProductEventConsumer(
    private val stockUseCase: StockUseCase,
) {
    @Order(1)
    @EventListener
    fun handleOrderCreatedEvent(event: OrderCreatedEvent) {
        event.products.forEach { productDetail ->
            val command =
                DecreaseStockCommand(
                    id = productDetail.productId,
                    quantity = productDetail.quantity,
                )

            stockUseCase.decreaseStock(command)
        }
    }
}
