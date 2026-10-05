package com.playground.product.application.service

import com.playground.product.application.port.inbound.StockUseCase
import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.application.port.inbound.command.StockConfirmCommand
import com.playground.product.application.port.inbound.command.StockReleaseCommand
import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.application.port.outbound.StockCachePort
import com.playground.product.domain.exception.InsufficientReservedStockException
import com.playground.product.domain.exception.InsufficientStockException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
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
        val quantitiesByProductId = verifiedQuantities(commands)
        val failedProductId = reserveStocks(orderId, quantitiesByProductId)
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

    private fun verifiedQuantities(commands: List<DecreaseStockCommand>): Map<Long, Int> =
        aggregateQuantities(commands.map { command ->
            productQueryPort.findById(command.id).id!! to command.quantity
        })

    private fun reserveStocks(orderId: Long, quantities: Map<Long, Int>): Long? {
        if (!hasActiveTransaction()) return stockCachePort.reserveStocks(orderId, quantities)

        val attemptId = UUID.randomUUID().toString()
        // Redis 요청이 실패해도 서버에서 예약했을 수 있으므로 호출 전에 보상을 등록한다.
        registerRollbackCompensation(orderId, quantities, attemptId)
        return stockCachePort.reserveStocksForTransaction(orderId, quantities, attemptId)
    }

    private fun hasActiveTransaction(): Boolean =
        TransactionSynchronizationManager.isActualTransactionActive() &&
            TransactionSynchronizationManager.isSynchronizationActive()

    private fun registerRollbackCompensation(orderId: Long, quantities: Map<Long, Int>, attemptId: String) {
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCompletion(status: Int) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    compensateReservation(orderId, quantities, attemptId)
                }
            }
        })
    }

    private fun compensateReservation(orderId: Long, quantities: Map<Long, Int>, attemptId: String) {
        try {
            val failedId = stockCachePort.releaseStocksForTransaction(orderId, quantities, attemptId)
            check(failedId == null) { "예약 보상 실패: 상품 $failedId" }
        } catch (exception: Exception) {
            // DB에 없는 주문은 자동 해제하지 않는다. 남은 예약은 별도 확인·보상이 필요하다.
            logger.error("재고 예약 보상 실패: orderId={}", orderId, exception)
        }
    }

    private fun aggregateQuantities(items: List<Pair<Long, Int>>): Map<Long, Int> =
        items.fold(linkedMapOf()) { quantities, (id, quantity) ->
            quantities.apply { this[id] = Math.addExact(this[id] ?: 0, quantity) }
        }
}
