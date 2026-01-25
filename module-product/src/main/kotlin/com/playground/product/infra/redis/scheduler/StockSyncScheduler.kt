package com.playground.product.infra.redis.scheduler

import com.playground.common.log.utils.logger
import com.playground.product.infra.redis.service.RedisStockService
import com.playground.product.persistence.repository.ProductRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Redis → DB 재고 동기화 스케줄러
 *
 * 10초마다 Redis에서 변경된 재고를 DB에 배치로 동기화합니다.
 * 더티 플래그를 사용하여 변경된 상품만 업데이트합니다.
 */
@Component
class StockSyncScheduler(
    private val redisStockService: RedisStockService,
    private val productRepository: ProductRepository,
) {
    private val logger = logger()

    /**
     * 10초마다 Redis → DB 재고 동기화
     *
     * Snapshot 기반으로 동작하여 동시성 안전성을 보장합니다.
     * 처리 시작 시점의 더티 플래그만 처리하고, 처리 완료된 것만 제거합니다.
     */
    @Scheduled(fixedRate = 10000)
    fun syncToDatabase() {
        try {
            // 1. 현재 시점의 더티 플래그 스냅샷 조회
            val snapshot = redisStockService.getDirtyProductIds()

            if (snapshot.isEmpty()) {
                logger.debug("변경된 재고 없음 - 동기화 스킵")
                return
            }

            logger.info("===== Redis → DB 재고 동기화 시작: ${snapshot.size}개 상품 =====")

            // 2. 스냅샷 기준 Redis 재고 조회
            val stockMap =
                snapshot.associateWith { productId ->
                    redisStockService.getStock(productId)
                }

            // 3. DB Batch UPDATE
            productRepository.batchUpdateStock(stockMap)

            // 4. 처리 완료된 상품만 더티 플래그 제거
            redisStockService.removeDirtyFlags(snapshot)

            logger.info("===== Redis → DB 재고 동기화 완료 =====")
        } catch (e: Exception) {
            logger.error("재고 동기화 실패: ${e.message}", e)
            // 실패해도 계속 진행 (다음 주기에 재시도)
        }
    }
}
