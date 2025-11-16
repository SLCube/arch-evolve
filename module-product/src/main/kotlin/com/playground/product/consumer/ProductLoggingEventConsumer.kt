package com.playground.product.consumer

import com.playground.common.log.utils.logger
import com.playground.product.domain.event.ProductCreatedEvent
import com.playground.product.domain.event.ProductStockDecreasedEvent
import com.playground.product.domain.event.ProductUpdatedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ProductLoggingEventConsumer {
    private val log = logger()

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleProductCreatedEvent(event: ProductCreatedEvent) {
        log.info(
            "Product created. productId={}, name={}, stock={}, price={}",
            event.productId,
            event.name,
            event.stock,
            event.price,
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleProductUpdatedEvent(event: ProductUpdatedEvent) {
        log.info(
            "Product updated. productId={}, oldName={}, newName={}, oldStock={}, newStock={}, oldPrice={}, newPrice={}",
            event.productId,
            event.oldName,
            event.newName,
            event.oldStock,
            event.newStock,
            event.oldPrice,
            event.newPrice,
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleProductStockDecreasedEvent(event: ProductStockDecreasedEvent) {
        log.info(
            "Product stock decreased. productId={}, productName={}, oldStock={}, decreasedQuantity={}, newStock={}",
            event.productId,
            event.productName,
            event.oldStock,
            event.decreasedQuantity,
            event.newStock,
        )
    }
}
