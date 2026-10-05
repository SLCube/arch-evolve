package com.playground.product.application.service

import com.playground.product.application.port.inbound.StockUseCase
import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.application.port.inbound.command.StockConfirmCommand
import com.playground.product.application.port.inbound.command.StockReleaseCommand
import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.application.port.outbound.StockCachePort
import com.playground.product.domain.exception.InsufficientReservedStockException
import com.playground.product.domain.exception.InsufficientStockException
import org.springframework.stereotype.Service

@Service
class StockService(
    private val productQueryPort: ProductQueryPort,
    private val stockCachePort: StockCachePort,
) : StockUseCase {
    override fun decreaseStocks(
        orderId: Long,
        commands: List<DecreaseStockCommand>,
    ) {
        val quantitiesByProductId = linkedMapOf<Long, Int>()
        commands.forEach { command ->
            val product = productQueryPort.findById(command.id)
            val productId = product.id!!
            quantitiesByProductId[productId] =
                Math.addExact(quantitiesByProductId[productId] ?: 0, command.quantity)
        }

        val failedProductId = stockCachePort.reserveStocks(orderId, quantitiesByProductId)
        if (failedProductId != null) {
            throw InsufficientStockException(failedProductId, quantitiesByProductId.getValue(failedProductId))
        }
    }

    override fun confirmStocks(
        orderId: Long,
        commands: List<StockConfirmCommand>,
    ) {
        commands.forEach { command ->
            val result = stockCachePort.confirmStock(command.productId, command.quantity)

            if (result < 0) {
                throw InsufficientReservedStockException(command.productId, command.quantity)
            }
        }
    }

    override fun releaseReservedStocks(
        orderId: Long,
        commands: List<StockReleaseCommand>,
    ) {
        commands.forEach { command ->
            val result = stockCachePort.releaseReservedStock(command.productId, command.quantity)

            if (result < 0) {
                throw InsufficientReservedStockException(command.productId, command.quantity)
            }
        }
    }
}
