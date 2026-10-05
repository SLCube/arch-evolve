package com.playground.product.application.scheduler

import com.playground.common.log.utils.logger
import com.playground.product.application.port.outbound.ProductCommandPort
import com.playground.product.application.port.outbound.StockCachePort
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 10초마다 Redis → DB 재고 동기화
 *
 * DB 저장 성공 후 확인한 확정 수량이 동일한 상품만 변경 표시를 제거한다.
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
            val snapshot = stockCachePort.getDirtyStockSnapshot()

            if (snapshot.isEmpty()) {
                logger.debug("변경된 재고 없음 - 동기화 스킵")
                return
            }

            logger.info("===== Redis → DB 재고 동기화 시작: ${snapshot.size}개 상품 =====")

            productCommandPort.batchUpdateStock(snapshot.mapValues { it.value.stock })
            stockCachePort.acknowledgeStockSync(snapshot)

            logger.info("===== Redis → DB 재고 동기화 완료 =====")
        } catch (e: Exception) {
            logger.error("재고 동기화 실패: ${e.message}", e)
        }
    }
}
