package com.playground.product.infra.redis.initializer

import com.playground.product.infra.redis.service.RedisStockService
import com.playground.product.persistence.repository.ProductRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * Redis 재고 초기화
 *
 * 애플리케이션 시작 시 DB의 모든 상품 재고를
 * Redis로 로딩합니다.
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

    override fun run(args: ApplicationArguments?) {
        logger.info("===== Redis 재고 초기화 시작 =====")

        try {
            val products = productRepository.findAll()

            products.forEach { product ->
                val productId = product.id!!
                val stock = product.stock

                redisStockService.setStock(productId, stock)
                logger.debug("재고 로딩: productId=$productId, stock=$stock")
            }

            logger.info("===== Redis 재고 초기화 완료: ${products.size}개 상품 =====")
        } catch (e: Exception) {
            logger.error("Redis 재고 초기화 실패: ${e.message}", e)
            throw e // 애플리케이션 시작 실패
        }
    }
}
