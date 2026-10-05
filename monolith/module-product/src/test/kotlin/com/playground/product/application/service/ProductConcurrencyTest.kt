package com.playground.product.application.service

import com.playground.product.application.port.inbound.StockUseCase
import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.domain.exception.InsufficientStockException
import com.playground.product.infra.redis.client.RedisStockClient
import com.playground.product.persistence.entity.ProductJpaEntity
import com.playground.product.persistence.repository.ProductRepository
import com.playground.support.IntegrationTestSupport
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
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
    fun `두 번째 상품의 재고가 부족하면 모든 상품의 예약 수량이 유지된다`() {
        // given - A는 100개, B는 5개이며 각각 10개를 요청한다.
        val secondProductId =
            productRepository.save(
                ProductJpaEntity(
                    name = "재고 부족 상품",
                    stock = 5,
                    price = 10000.toBigDecimal(),
                ),
            ).id!!
        redisStockClient.setStock(secondProductId, 5)
        val commands =
            listOf(
                DecreaseStockCommand(productId, 10),
                DecreaseStockCommand(secondProductId, 10),
            )

        // when
        shouldThrow<InsufficientStockException> {
            stockUseCase.decreaseStocks(orderId = 123L, commands = commands)
        }

        // then
        assertSoftly {
            redisStockClient.getReservedStock(productId) shouldBe 0
            redisStockClient.getReservedStock(secondProductId) shouldBe 0
            redisStockClient.getStock(productId) shouldBe 100
            redisStockClient.getStock(secondProductId) shouldBe 5
        }
    }

    @Test
    fun `동일 상품의 합산 요청 수량이 재고를 초과하면 예약을 남기지 않는다`() {
        val commands =
            listOf(
                DecreaseStockCommand(productId, 60),
                DecreaseStockCommand(productId, 60),
            )

        shouldThrow<InsufficientStockException> {
            stockUseCase.decreaseStocks(orderId = 123L, commands = commands)
        }

        redisStockClient.getReservedStock(productId) shouldBe 0
        redisStockClient.getStock(productId) shouldBe 100
    }

    @Test
    fun `동일 상품의 요청 수량을 합산하여 예약한다`() {
        stockUseCase.decreaseStocks(
            orderId = 123L,
            commands = listOf(
                DecreaseStockCommand(productId, 10),
                DecreaseStockCommand(productId, 20),
            ),
        )

        redisStockClient.getReservedStock(productId) shouldBe 30
        redisStockClient.getStock(productId) shouldBe 70
    }

    @Test
    fun `동시에 100개의 재고를 차감하면, 최종 재고는 0이 된다`() {
        val threadCount = 100
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)

        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    stockUseCase.decreaseStocks(
                        orderId = i.toLong(),
                        commands = listOf(DecreaseStockCommand(productId, 1)),
                    )
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
        val dirtyIds = redisStockClient.getDirtyProductIdsAndClear()
        dirtyIds shouldBe emptySet()
    }
}
