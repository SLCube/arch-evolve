package com.playground.product.infra.redis.client

import io.kotest.matchers.collections.shouldContain
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@Suppress("NonAsciiCharacters")
@Testcontainers
@SpringBootTest
class RedisStockClientTest(
    @param:Autowired private val redisStockClient: RedisStockClient,
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
        // Redis 초기화
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
    }

    @AfterEach
    fun tearDown() {
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
    }

    @Test
    fun `재고 설정 및 조회가 정상 동작한다`() {
        // given
        val productId = 1L
        val stock = 100

        // when
        redisStockClient.setStock(productId, stock)
        val result = redisStockClient.getStock(productId)

        // then
        result shouldBe stock
    }

    @Test
    fun `재고 차감이 정상 동작한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)

        // when
        val remaining = redisStockClient.decreaseStock(productId, 30)

        // then
        remaining shouldBe 70
        redisStockClient.getStock(productId) shouldBe 70
    }

    @Test
    fun `재고 부족 시 -1을 반환한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 10)

        // when
        val result = redisStockClient.decreaseStock(productId, 20)

        // then
        result shouldBe -1
        redisStockClient.getStock(productId) shouldBe 10 // 재고는 변경되지 않음
    }

    @Test
    fun `재고 차감 시 더티 플래그가 추가된다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)

        // when
        redisStockClient.decreaseStock(productId, 10)

        // then
        val dirtyIds = redisStockClient.getDirtyProductIds()
        dirtyIds shouldContain productId
    }

    @Test
    fun `더티 플래그 개별 제거가 정상 동작한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)
        redisStockClient.decreaseStock(productId, 10)

        // when
        redisStockClient.removeDirtyFlags(setOf(productId))

        // then
        val dirtyIds = redisStockClient.getDirtyProductIds()
        dirtyIds.size shouldBe 0
    }

    @Test
    fun `100개 스레드로 동시에 재고를 차감하면 최종 재고는 0이 된다`() {
        // given
        val productId = 1L
        val initialStock = 100
        redisStockClient.setStock(productId, initialStock)

        val threadCount = 100
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)

        // when
        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    redisStockClient.decreaseStock(productId, 1)
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executorService.shutdown()

        // then
        val finalStock = redisStockClient.getStock(productId)
        finalStock shouldBe 0
    }

    @Test
    fun `동시 요청 시 재고 부족으로 일부 요청은 실패한다`() {
        // given
        val productId = 1L
        val initialStock = 50
        redisStockClient.setStock(productId, initialStock)

        val threadCount = 100
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)
        val successCount = java.util.concurrent.atomic.AtomicInteger(0)

        // when
        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    val result = redisStockClient.decreaseStock(productId, 1)
                    if (result >= 0) {
                        successCount.incrementAndGet()
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executorService.shutdown()

        // then
        successCount.get() shouldBe initialStock
        redisStockClient.getStock(productId) shouldBe 0
    }

    @Test
    fun `여러 상품의 재고를 차감하면 모든 상품이 더티 플래그에 추가된다`() {
        // given
        val productIds = listOf(1L, 2L, 3L)
        productIds.forEach { productId ->
            redisStockClient.setStock(productId, 100)
        }

        // when
        productIds.forEach { productId ->
            redisStockClient.decreaseStock(productId, 10)
        }

        // then
        val dirtyIds = redisStockClient.getDirtyProductIds()
        dirtyIds.size shouldBe productIds.size
        productIds.forEach { productId ->
            dirtyIds shouldContain productId
        }
    }

    // 3단계 재고 관리 테스트
    @Test
    fun `재고 예약이 정상 동작한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)

        // when
        val remainingStock = redisStockClient.reserveStock(productId, 30)

        // then
        remainingStock shouldBe 70
        redisStockClient.getAvailableStock(productId) shouldBe 100
        redisStockClient.getReservedStock(productId) shouldBe 30
        redisStockClient.getConfirmedStock(productId) shouldBe 0
        redisStockClient.getStock(productId) shouldBe 70 // available - reserved - confirmed
    }

    @Test
    fun `재고 예약 시 판매 가능 재고가 부족하면 -1을 반환한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 10)

        // when
        val result = redisStockClient.reserveStock(productId, 20)

        // then
        result shouldBe -1
        redisStockClient.getReservedStock(productId) shouldBe 0 // 예약되지 않음
    }

    @Test
    fun `재고 확정 시 reserved가 감소하고 confirmed가 증가한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)
        redisStockClient.reserveStock(productId, 30)

        // when
        val confirmedStock = redisStockClient.confirmStock(productId, 30)

        // then
        confirmedStock shouldBe 30
        redisStockClient.getAvailableStock(productId) shouldBe 100
        redisStockClient.getReservedStock(productId) shouldBe 0
        redisStockClient.getConfirmedStock(productId) shouldBe 30
        redisStockClient.getStock(productId) shouldBe 70 // 100 - 0 - 30
    }

    @Test
    fun `재고 확정 시 더티 플래그가 추가된다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)
        redisStockClient.reserveStock(productId, 30)

        // when
        redisStockClient.confirmStock(productId, 30)

        // then
        val dirtyIds = redisStockClient.getDirtyProductIds()
        dirtyIds shouldContain productId
    }

    @Test
    fun `예약 해제 시 reserved가 감소한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)
        redisStockClient.reserveStock(productId, 30)

        // when
        val reservedStock = redisStockClient.releaseReservedStock(productId, 30)

        // then
        reservedStock shouldBe 0
        redisStockClient.getAvailableStock(productId) shouldBe 100
        redisStockClient.getReservedStock(productId) shouldBe 0
        redisStockClient.getConfirmedStock(productId) shouldBe 0
        redisStockClient.getStock(productId) shouldBe 100 // 원복
    }

    @Test
    fun `예약 해제 시 더티 플래그가 추가되지 않는다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)
        redisStockClient.reserveStock(productId, 30)

        // when
        redisStockClient.releaseReservedStock(productId, 30)

        // then
        val dirtyIds = redisStockClient.getDirtyProductIds()
        dirtyIds.size shouldBe 0
    }

    @Test
    fun `3단계 재고 관리 - 전체 플로우가 정상 동작한다`() {
        // given
        val productId = 1L
        val initialStock = 100
        redisStockClient.setStock(productId, initialStock)

        // when - 1. 주문 생성 (예약)
        redisStockClient.reserveStock(productId, 30)

        // then - 1
        redisStockClient.getStock(productId) shouldBe 70 // 100 - 30 - 0

        // when - 2. 결제 성공 (확정)
        redisStockClient.confirmStock(productId, 30)

        // then - 2
        redisStockClient.getStock(productId) shouldBe 70 // 100 - 0 - 30
        redisStockClient.getReservedStock(productId) shouldBe 0
        redisStockClient.getConfirmedStock(productId) shouldBe 30

        // when - 3. 다른 주문 생성 후 결제 실패 (예약 해제)
        redisStockClient.reserveStock(productId, 20)
        redisStockClient.releaseReservedStock(productId, 20)

        // then - 3
        redisStockClient.getStock(productId) shouldBe 70 // 100 - 0 - 30 (변화 없음)
        redisStockClient.getReservedStock(productId) shouldBe 0
    }

    @Test
    fun `100개 스레드로 동시에 재고를 예약하면 정합성이 보장된다`() {
        // given
        val productId = 1L
        val initialStock = 100
        redisStockClient.setStock(productId, initialStock)

        val threadCount = 100
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)
        val successCount = java.util.concurrent.atomic.AtomicInteger(0)

        // when
        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    val result = redisStockClient.reserveStock(productId, 1)
                    if (result >= 0) {
                        successCount.incrementAndGet()
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executorService.shutdown()

        // then
        successCount.get() shouldBe initialStock
        redisStockClient.getStock(productId) shouldBe 0 // 모두 예약됨
        redisStockClient.getReservedStock(productId) shouldBe 100
    }

    @Test
    fun `동시 예약-확정 시 재고 정합성이 보장된다`() {
        // given
        val productId = 1L
        val initialStock = 100
        redisStockClient.setStock(productId, initialStock)

        val threadCount = 100
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)
        val successCount = java.util.concurrent.atomic.AtomicInteger(0)

        // when - 예약 후 즉시 확정
        for (i in 1..threadCount) {
            executorService.submit {
                try {
                    val reserveResult = redisStockClient.reserveStock(productId, 1)
                    if (reserveResult >= 0) {
                        redisStockClient.confirmStock(productId, 1)
                        successCount.incrementAndGet()
                    }
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executorService.shutdown()

        // then
        successCount.get() shouldBe initialStock
        redisStockClient.getStock(productId) shouldBe 0
        redisStockClient.getReservedStock(productId) shouldBe 0
        redisStockClient.getConfirmedStock(productId) shouldBe 100
    }
}
