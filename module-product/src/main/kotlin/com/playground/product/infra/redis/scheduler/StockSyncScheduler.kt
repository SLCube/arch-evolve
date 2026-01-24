package com.playground.product.infra.redis.scheduler

import com.playground.product.infra.redis.service.RedisStockService
import com.playground.product.persistence.repository.ProductRepository
import org.slf4j.LoggerFactory
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
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * 10초마다 Redis → DB 재고 동기화
     */
    @Scheduled(fixedRate = 10000)
    fun syncToDatabase() {
        try {
            val dirtyProductIds = redisStockService.getDirtyProductIds()

            if (dirtyProductIds.isEmpty()) {
                logger.debug("변경된 재고 없음 - 동기화 스킵")
                return
            }

            logger.info("===== Redis → DB 재고 동기화 시작: ${dirtyProductIds.size}개 상품 =====")

            // Redis에서 현재 재고 조회
            val stockMap =
                dirtyProductIds.associateWith { productId ->
                    redisStockService.getStock(productId)
                }

            // Batch UPDATE
            productRepository.batchUpdateStock(stockMap)

            // 더티 플래그 초기화
            redisStockService.clearDirtyFlags()

            logger.info("===== Redis → DB 재고 동기화 완료 =====")
        } catch (e: Exception) {
            logger.error("재고 동기화 실패: ${e.message}", e)
            // 실패해도 계속 진행 (다음 주기에 재시도)
        }
    }
}
