package com.playground.product.application.service

import com.playground.order.contract.port.OrderStockOutcome
import com.playground.order.contract.port.OrderStockRecoveryPort
import com.playground.product.application.port.outbound.StockCachePort
import org.slf4j.LoggerFactory

class StockReservationRecoveryService(
    private val stock: StockCachePort,
    private val orders: OrderStockRecoveryPort,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val RECOVERY_BATCH_SIZE = 100
        private const val RETRY_DELAY_MILLIS = 30_000L
    }

    fun recover(nowMillis: Long = System.currentTimeMillis()) {
        stock.getReservationsForRecovery(nowMillis, RECOVERY_BATCH_SIZE).forEach { reservation ->
            try {
                // 처리 실패·PENDING도 다음 시도 시각을 갱신하여 특정 주문이 배치를 독점하지 않는다.
                stock.deferReservationRecovery(reservation.orderId, nowMillis + RETRY_DELAY_MILLIS)
                val failedId = when (orders.inspect(reservation.orderId)) {
                    OrderStockOutcome.CONFIRMED -> stock.confirmStocks(reservation.orderId, reservation.quantities)
                    OrderStockOutcome.RELEASED -> stock.releaseStocks(reservation.orderId, reservation.quantities)
                    OrderStockOutcome.PENDING, OrderStockOutcome.UNKNOWN -> null
                }
                check(failedId == null) { "예약 복구 실패: 상품 $failedId" }
            } catch (exception: Exception) {
                logger.error("재고 예약 복구 실패: orderId={}", reservation.orderId, exception)
            }
        }
    }
}
