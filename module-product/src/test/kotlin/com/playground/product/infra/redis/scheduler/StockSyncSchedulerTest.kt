package com.playground.product.infra.redis.scheduler

import com.playground.product.infra.redis.service.RedisStockService
import com.playground.product.persistence.entity.ProductJpaEntity
import com.playground.product.persistence.repository.ProductRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Suppress("NonAsciiCharacters")
@Testcontainers
@SpringBootTest
class StockSyncSchedulerTest(
    @param:Autowired private val stockSyncScheduler: StockSyncScheduler,
    @param:Autowired private val redisStockService: RedisStockService,
    @param:Autowired private val productRepository: ProductRepository,
    @param:Autowired private val redisTemplate: RedisTemplate<String, String>,
) {
    companion object {
        @Container
        @JvmStatic
        private val redis =
            GenericContainer<Nothing>("redis:7-alpine").apply {
                withExposedPorts(6379)
            }

        @Container
        @JvmStatic
        private val postgres =
            PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
                withDatabaseName("testdb")
                withUsername("test")
                withPassword("test")
            }

        @DynamicPropertySource
        @JvmStatic
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host") { redis.host }
            registry.add("spring.data.redis.port") { redis.firstMappedPort }
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("app.redis.stock.initializer.enabled") { "false" }
        }
    }

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
        redisStockService.setStock(productId, 50)

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

        // Redis 재고 설정 및 차감 (더티 플래그 생성)
        redisStockService.setStock(productId, 100)
        redisStockService.decreaseStockIfAvailable(productId, 30)

        // when
        stockSyncScheduler.syncToDatabase()

        // then - DB 재고가 Redis와 동기화됨
        val dbProduct = productRepository.findById(productId).get()
        dbProduct.stock shouldBe 70

        // 더티 플래그 초기화 확인
        val dirtyIds = redisStockService.getDirtyProductIds()
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

        // Redis 재고 설정 및 차감
        productIds.forEachIndexed { index, productId ->
            redisStockService.setStock(productId, (index + 1) * 100)
            redisStockService.decreaseStockIfAvailable(productId, 10)
        }

        // when
        stockSyncScheduler.syncToDatabase()

        // then - 모든 상품의 DB 재고가 동기화됨
        val dbProducts = productRepository.findAllById(productIds)
        dbProducts[0].stock shouldBe 90 // 100 - 10
        dbProducts[1].stock shouldBe 190 // 200 - 10
        dbProducts[2].stock shouldBe 290 // 300 - 10

        // 더티 플래그 초기화 확인
        val dirtyIds = redisStockService.getDirtyProductIds()
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
        redisStockService.setStock(productId, 1000)

        // 여러 번 재고 차감
        repeat(10) {
            redisStockService.decreaseStockIfAvailable(productId, 50)
        }

        // when - 동기화
        stockSyncScheduler.syncToDatabase()

        // then - Redis와 DB 재고가 일치
        val redisStock = redisStockService.getStock(productId)
        val dbStock = productRepository.findById(productId).get().stock

        redisStock shouldBe 500
        dbStock shouldBe 500
    }
}
