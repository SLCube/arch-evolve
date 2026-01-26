package com.playground.product.application.initializer

import com.playground.common.application.query.PageQuery
import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.application.port.outbound.StockCachePort
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

/**
 * 애플리케이션 시작 시 DB 상품 재고를 Cache로 초기화
 *
 * Paging + MSET으로 대량 데이터 처리 (메모리 안정성 + 네트워크 효율)
 */
@Component
@ConditionalOnProperty(
    prefix = "app.redis.stock",
    name = ["initializer.enabled"],
    havingValue = "true",
    matchIfMissing = true,
)
class StockCacheInitializer(
    private val productQueryPort: ProductQueryPort,
    private val stockCachePort: StockCachePort,
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val PAGE_SIZE = 10000 // 메모리-성능 트레이드오프 균형점 (실험 결과)
    }

    override fun run(args: ApplicationArguments?) {
        val startTime = System.currentTimeMillis()
        logger.info("===== 재고 캐시 초기화 시작 =====")

        try {
            var pageNumber = 0
            var totalCount = 0

            do {
                val pageQuery = PageQuery(pageNumber, PAGE_SIZE)
                val page = productQueryPort.findAll(pageQuery)

                if (page.content.isEmpty()) {
                    break
                }

                val stockMap =
                    page.content.associate { product ->
                        product.id!! to product.stock
                    }

                stockCachePort.setStockBatch(stockMap)

                totalCount += page.content.size
                pageNumber++
            } while (pageNumber < page.totalPages)

            val duration = System.currentTimeMillis() - startTime
            logger.info("===== 재고 캐시 초기화 완료: ${totalCount}개 상품, ${duration}ms =====")
        } catch (e: Exception) {
            logger.error("재고 캐시 초기화 실패: ${e.message}", e)
            throw e
        }
    }
}
