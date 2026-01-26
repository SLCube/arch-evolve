package com.playground.product.application.scheduler

import com.playground.common.log.utils.logger
import com.playground.product.application.port.outbound.ProductCommandPort
import com.playground.product.application.port.outbound.StockCachePort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 10초마다 Redis → DB 재고 동기화
 *
 * Snapshot 기반 처리로 동시성 안전성 보장
 * (동기화 중 새로운 재고 차감 발생해도 누락 방지)
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
            val snapshot = stockCachePort.getDirtyProductIds()

            if (snapshot.isEmpty()) {
                logger.debug("변경된 재고 없음 - 동기화 스킵")
                return
            }

            logger.info("===== Redis → DB 재고 동기화 시작: ${snapshot.size}개 상품 =====")

            val stockMap =
                snapshot.associateWith { productId ->
                    stockCachePort.getStock(productId)
                }

            productCommandPort.batchUpdateStock(stockMap)
            stockCachePort.removeDirtyFlags(snapshot)

            logger.info("===== Redis → DB 재고 동기화 완료 =====")
        } catch (e: Exception) {
            logger.error("재고 동기화 실패: ${e.message}", e)
        }
    }
}
