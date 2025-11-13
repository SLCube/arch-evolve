package com.playground.product.consumer

import com.playground.order.domain.event.OrderCreatedEvent
import com.playground.product.application.port.`in`.ProductUseCase
import com.playground.product.application.port.`in`.command.DecreaseStockCommand
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ProductEventListener(
    private val productUseCase: ProductUseCase
) {

    @EventListener
    fun handleOrderCreatedEvent(event: OrderCreatedEvent) {
        event.products.forEach { productDetail ->
            val command = DecreaseStockCommand(
                id = productDetail.productId,
                quantity = productDetail.quantity
            )

            productUseCase.decreaseStock(command)
        }
    }
}