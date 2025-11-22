package com.playground.product.consumer

import com.playground.order.contract.domain.event.OrderCreatedEvent
import com.playground.product.application.port.inbound.ProductUseCase
import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductEventConsumer(
    private val productUseCase: ProductUseCase,
) {
    @EventListener
    @Transactional
    fun handleOrderCreatedEvent(event: OrderCreatedEvent) {
        event.products.forEach { productDetail ->
            val command =
                DecreaseStockCommand(
                    id = productDetail.productId,
                    quantity = productDetail.quantity,
                )

            productUseCase.decreaseStock(command)
        }
    }
}
