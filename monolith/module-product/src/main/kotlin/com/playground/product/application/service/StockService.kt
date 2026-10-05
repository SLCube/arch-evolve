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
import org.slf4j.LoggerFactory
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.UUID

@Service
class StockService(
    private val productQueryPort: ProductQueryPort,
    private val stockCachePort: StockCachePort,
) : StockUseCase {
    private val logger = LoggerFactory.getLogger(javaClass)
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

        val failedProductId =
            if (TransactionSynchronizationManager.isActualTransactionActive() &&
                TransactionSynchronizationManager.isSynchronizationActive()
            ) {
                val attemptId = UUID.randomUUID().toString()
                TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                    override fun afterCompletion(status: Int) {
                        if (status != TransactionSynchronization.STATUS_ROLLED_BACK) return
                        try {
                            val failedId = stockCachePort.releaseStocksForTransaction(orderId, quantitiesByProductId, attemptId)
                            check(failedId == null) { "예약 보상 실패: 상품 $failedId" }
                        } catch (exception: Exception) {
                            // 예약 기록은 삭제하지 않는다. 재조정 작업에서 커밋 결과를 확인하고 재시도한다.
                            logger.error("재고 예약 보상 실패: orderId={}", orderId, exception)
                        }
                    }
                })
                stockCachePort.reserveStocksForTransaction(orderId, quantitiesByProductId, attemptId)
            } else {
                stockCachePort.reserveStocks(orderId, quantitiesByProductId)
            }
        if (failedProductId != null) {
            throw InsufficientStockException(failedProductId, quantitiesByProductId.getValue(failedProductId))
        }
    }

    override fun confirmStocks(
        orderId: Long,
        commands: List<StockConfirmCommand>,
    ) {
        val quantities = aggregateQuantities(commands.map { it.productId to it.quantity })
        val failedId = stockCachePort.confirmStocks(orderId, quantities)
        if (failedId != null) {
            throw InsufficientReservedStockException(failedId, quantities.getValue(failedId))
        }
    }

    override fun releaseReservedStocks(
        orderId: Long,
        commands: List<StockReleaseCommand>,
    ) {
        val quantities = aggregateQuantities(commands.map { it.productId to it.quantity })
        val failedId = stockCachePort.releaseStocks(orderId, quantities)
        if (failedId != null) {
            throw InsufficientReservedStockException(failedId, quantities.getValue(failedId))
        }
    }

    private fun aggregateQuantities(items: List<Pair<Long, Int>>): Map<Long, Int> =
        items.fold(linkedMapOf()) { quantities, (id, quantity) ->
            quantities.apply { this[id] = Math.addExact(this[id] ?: 0, quantity) }
        }
}
