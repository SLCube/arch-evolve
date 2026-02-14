package com.playground.product.application.scheduler

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

@Suppress("NonAsciiCharacters")
@SpringBootTest
class StockSyncSchedulerTest(
    @param:Autowired private val stockSyncScheduler: StockSyncScheduler,
    @param:Autowired private val redisStockClient: RedisStockClient,
    @param:Autowired private val productRepository: ProductRepository,
    @param:Autowired private val redisTemplate: RedisTemplate<String, String>,
) : IntegrationTestSupport() {

    @BeforeEach
    fun setUp() {
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
        productRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
        productRepository.deleteAll()
    }

    @Test
    fun `더티 플래그가 없으면 동기화를 스킵한다`() {
        // given
        val product =
            productRepository.save(
                ProductJpaEntity(
                    name = "테스트 상품",
                    stock = 100,
                    price = 10000.toBigDecimal(),
                ),
            )
        val productId = product.id!!

        // Redis 재고 설정 (더티 플래그 없음)
        redisStockClient.setStock(productId, 50)

        // when
        stockSyncScheduler.syncToDatabase()

        // then - DB 재고는 변경되지 않음
        val dbProduct = productRepository.findById(productId).get()
        dbProduct.stock shouldBe 100
    }

    @Test
    fun `더티 플래그가 있으면 DB에 동기화한다`() {
        // given
        val product =
            productRepository.save(
                ProductJpaEntity(
                    name = "테스트 상품",
                    stock = 100,
                    price = 10000.toBigDecimal(),
                ),
            )
        val productId = product.id!!

        // Redis 재고 설정, 예약 및 확정 (더티 플래그 생성)
        redisStockClient.setStock(productId, 100)
        redisStockClient.reserveStock(productId, 30)
        redisStockClient.confirmStock(productId, 30)

        // when
        stockSyncScheduler.syncToDatabase()

        // then - DB 재고가 Redis와 동기화됨
        val dbProduct = productRepository.findById(productId).get()
        dbProduct.stock shouldBe 70

        // 더티 플래그 초기화 확인
        val dirtyIds = redisStockClient.getDirtyProductIds()
        dirtyIds.size shouldBe 0
    }

    @Test
    fun `여러 상품의 재고를 배치로 동기화한다`() {
        // given
        val products =
            listOf(
                ProductJpaEntity(name = "상품1", stock = 100, price = 10000.toBigDecimal()),
                ProductJpaEntity(name = "상품2", stock = 200, price = 20000.toBigDecimal()),
                ProductJpaEntity(name = "상품3", stock = 300, price = 30000.toBigDecimal()),
            )
        val savedProducts = productRepository.saveAll(products)
        val productIds = savedProducts.map { it.id!! }

        // Redis 재고 설정, 예약 및 확정
        productIds.forEachIndexed { index, productId ->
            redisStockClient.setStock(productId, (index + 1) * 100)
            redisStockClient.reserveStock(productId, 10)
            redisStockClient.confirmStock(productId, 10)
        }

        // when
        stockSyncScheduler.syncToDatabase()

        // then - 모든 상품의 DB 재고가 동기화됨
        val dbProducts = productRepository.findAllById(productIds)
        dbProducts[0].stock shouldBe 90 // 100 - 10
        dbProducts[1].stock shouldBe 190 // 200 - 10
        dbProducts[2].stock shouldBe 290 // 300 - 10

        // 더티 플래그 초기화 확인
        val dirtyIds = redisStockClient.getDirtyProductIds()
        dirtyIds.size shouldBe 0
    }

    @Test
    fun `Redis와 DB의 재고가 정합성을 유지한다`() {
        // given
        val product =
            productRepository.save(
                ProductJpaEntity(
                    name = "테스트 상품",
                    stock = 1000,
                    price = 10000.toBigDecimal(),
                ),
            )
        val productId = product.id!!

        // Redis 재고 초기화
        redisStockClient.setStock(productId, 1000)

        // 여러 번 재고 예약 및 확정
        repeat(10) {
            redisStockClient.reserveStock(productId, 50)
            redisStockClient.confirmStock(productId, 50)
        }

        // when - 동기화
        stockSyncScheduler.syncToDatabase()

        // then - Redis와 DB 재고가 일치
        val redisStock = redisStockClient.getStock(productId)
        val dbStock = productRepository.findById(productId).get().stock

        redisStock shouldBe 500
        dbStock shouldBe 500
    }
}
