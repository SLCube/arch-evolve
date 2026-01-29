package com.playground.product.application.service.customer

import com.playground.product.application.port.inbound.StockUseCase
import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.application.port.inbound.command.StockConfirmCommand
import com.playground.product.application.port.inbound.command.StockReleaseCommand
import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.application.port.outbound.StockCachePort
import com.playground.product.domain.exception.InsufficientStockException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StockService(
    private val productQueryPort: ProductQueryPort,
    private val stockCachePort: StockCachePort,
) : StockUseCase {
    override fun decreaseStock(command: DecreaseStockCommand): Long {
        val product = productQueryPort.findById(command.id)

        val productId = product.id!!
        val decreasedQuantity = command.quantity

        val result = stockCachePort.reserveStock(productId, decreasedQuantity)

        if (result < 0) {
            throw InsufficientStockException(productId, decreasedQuantity)
        }

        return productId
    }

    override fun confirmStock(command: StockConfirmCommand): Long {
        return stockCachePort.confirmStock(command.productId, command.quantity)
    }

    override fun releaseReservedStock(command: StockReleaseCommand): Long {
        return stockCachePort.releaseReservedStock(command.productId, command.quantity)
    }
}
