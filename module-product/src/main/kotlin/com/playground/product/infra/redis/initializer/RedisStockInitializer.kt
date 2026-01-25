package com.playground.product.infra.redis.initializer

import com.playground.product.infra.redis.service.RedisStockService
import com.playground.product.persistence.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

/**
 * Redis 재고 초기화
 *
 * 애플리케이션 시작 시 DB의 모든 상품 재고를
 * Redis로 로딩합니다.
 *
 * Paging + MSET 방식으로 대량 데이터를 효율적으로 처리합니다.
 * - 메모리: 페이지 단위로 처리하여 OOM 방지
 * - 네트워크: MSET으로 일괄 전송하여 성능 향상
 *
 * 테스트 환경에서는 비활성화됩니다.
 */
@Component
@ConditionalOnProperty(
    prefix = "app.redis.stock",
    name = ["initializer.enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class RedisStockInitializer(
    private val productRepository: ProductRepository,
    private val redisStockService: RedisStockService,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val PAGE_SIZE = 10000 // 페이지당 처리 개수
    }

    override fun run(args: ApplicationArguments?) {
        val startTime = System.currentTimeMillis()
        logger.info("===== Redis 재고 초기화 시작 =====")

        try {
            var pageNumber = 0
            var totalCount = 0

            do {
                val pageable = PageRequest.of(pageNumber, PAGE_SIZE)
                val page = productRepository.findAll(pageable)

                if (page.isEmpty) {
                    break
                }

                val stockMap =
                    page.content.associate { product ->
                        product.id!! to product.stock
                    }

                redisStockService.setStockBatch(stockMap)

                totalCount += page.numberOfElements
                pageNumber++
            } while (page.hasNext())

            val duration = System.currentTimeMillis() - startTime
            logger.info("===== Redis 재고 초기화 완료: ${totalCount}개 상품, ${duration}ms =====")
        } catch (e: Exception) {
            logger.error("Redis 재고 초기화 실패: ${e.message}", e)
            throw e // 애플리케이션 시작 실패
        }
    }
}
