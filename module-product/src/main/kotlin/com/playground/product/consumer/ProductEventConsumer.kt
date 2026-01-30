package com.playground.product.consumer

import com.playground.order.contract.domain.event.OrderCompletedEvent
import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.order.contract.domain.event.OrderFailedEvent
import com.playground.product.application.port.inbound.StockUseCase
import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.application.port.inbound.command.StockConfirmCommand
import com.playground.product.application.port.inbound.command.StockReleaseCommand
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
        val commands =
            event.products.map { productDetail ->
                DecreaseStockCommand(
                    id = productDetail.productId,
                    quantity = productDetail.quantity,
                )
            }
        stockUseCase.decreaseStocks(commands)
    }

    @EventListener
    fun handleOrderCompletedEvent(event: OrderCompletedEvent) {
        val commands =
            event.products.map { product ->
                StockConfirmCommand(
                    productId = product.productId,
                    quantity = product.quantity,
                )
            }
        stockUseCase.confirmStocks(commands)
    }

    @EventListener
    fun handleOrderFailedEvent(event: OrderFailedEvent) {
        val commands =
            event.products.map { product ->
                StockReleaseCommand(
                    productId = product.productId,
                    quantity = product.quantity,
                )
            }
        stockUseCase.releaseReservedStocks(commands)
    }
}
