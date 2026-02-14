package com.playground.product.application.service

import com.playground.product.application.port.inbound.StockUseCase
import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.infra.redis.client.RedisStockClient
import com.playground.product.persistence.entity.ProductJpaEntity
import com.playground.product.persistence.repository.ProductRepository
import com.playground.support.IntegrationTestSupport
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@Suppress("NonAsciiCharacters")
@SpringBootTest
class ProductConcurrencyTest(
    @param:Autowired private val stockUseCase: StockUseCase,
    @param:Autowired private val productRepository: ProductRepository,
    @param:Autowired private val redisStockClient: RedisStockClient,
    @param:Autowired private val redisTemplate: RedisTemplate<String, String>,
) : IntegrationTestSupport() {
    private var productId: Long = 0L

    @BeforeEach
    fun setUp() {
        // Redis 초기화
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()

        // 상품 생성
        val productJpaEntity =
            productRepository.save(
                ProductJpaEntity(
                    name = "테스트 상품",
                    stock = 100,
                    price = 10000.toBigDecimal(),
                ),
            )
        productId = productJpaEntity.id!!

        // Redis에 재고 설정
        redisStockClient.setStock(productId, 100)
    }

    @AfterEach
    fun tearDown() {
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
        productRepository.deleteAll()
    }

    @Test
    fun `동시에 100개의 재고를 차감하면, 최종 재고는 0이 된다`() {
        val threadCount = 100
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)

        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    stockUseCase.decreaseStocks(listOf(DecreaseStockCommand(productId, 1)))
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executorService.shutdown()

        // Redis 재고 확인 (실시간 재고)
        val redisStock = redisStockClient.getStock(productId)
        redisStock shouldBe 0

        // Phase 3: 재고 예약(reserve) 시에는 dirty 플래그를 추가하지 않음
        // 확정(confirm) 시에만 dirty 플래그 추가
        val dirtyIds = redisStockClient.getDirtyProductIds()
        dirtyIds shouldBe emptySet()
    }
}