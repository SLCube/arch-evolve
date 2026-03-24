package com.playground.product.application.scheduler

import com.playground.common.log.utils.logger
import com.playground.product.application.port.outbound.ProductCommandPort
import com.playground.product.application.port.outbound.StockCachePort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 10초마다 Redis → DB 재고 동기화
 *
 * Lua script 기반 원자적 처리로 race condition 방지
 * (getDirtyProductIdsAndClear: SMEMBERS + DEL을 단일 Lua script로 원자적 실행)
 */
@Component
class StockSyncScheduler(
    private val stockCachePort: StockCachePort,
    private val productCommandPort: ProductCommandPort,
) {
    private val logger = logger()

    @Scheduled(fixedRate = 10000)
    fun syncToDatabase() {
        try {
            val snapshot = stockCachePort.getDirtyProductIdsAndClear()

            if (snapshot.isEmpty()) {
                logger.debug("변경된 재고 없음 - 동기화 스킵")
                return
            }

            logger.info("===== Redis → DB 재고 동기화 시작: ${snapshot.size}개 상품 =====")

            // Phase 4: available - confirmed로 DB 동기화
            // reserved는 일시적이므로 제외
            val stockMap =
                snapshot.associateWith { productId ->
                    val available = stockCachePort.getAvailableStock(productId)
                    val confirmed = stockCachePort.getConfirmedStock(productId)
                    available - confirmed
                }

            productCommandPort.batchUpdateStock(stockMap)

            logger.info("===== Redis → DB 재고 동기화 완료 =====")
        } catch (e: Exception) {
            logger.error("재고 동기화 실패: ${e.message}", e)
        }
    }
}
