package com.playground.product.infra.redis.client

import com.playground.support.IntegrationTestSupport
import com.playground.product.domain.exception.StockReservationConflictException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Suppress("NonAsciiCharacters")
@SpringBootTest
class RedisStockClientTest(
    @param:Autowired private val redisStockClient: RedisStockClient,
    @param:Autowired private val redisTemplate: RedisTemplate<String, String>,
) : IntegrationTestSupport() {

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
    fun `재시작 초기화는 기존 기준 재고와 예약 확정 수량을 덮어쓰지 않는다`() {
        redisStockClient.setStockBatch(mapOf(1L to 100))
        redisStockClient.reserveStocks(1L, mapOf(1L to 10))
        redisStockClient.reserveStocks(2L, mapOf(1L to 20))
        redisStockClient.confirmStocks(1L, mapOf(1L to 10))
        redisStockClient.setStockBatch(mapOf(1L to 90))

        redisStockClient.getAvailableStock(1L) shouldBe 100
        redisStockClient.getReservedStock(1L) shouldBe 20
        redisStockClient.getConfirmedStock(1L) shouldBe 10
        redisStockClient.getStock(1L) shouldBe 70
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

    // 3단계 재고 관리 테스트
    @Test
    fun `재고 예약이 정상 동작한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)

        // when
        val failedId = redisStockClient.reserveStocks(1L, mapOf(productId to 30))

        // then
        failedId shouldBe null
        redisStockClient.getAvailableStock(productId) shouldBe 100
        redisStockClient.getReservedStock(productId) shouldBe 30
        redisStockClient.getConfirmedStock(productId) shouldBe 0
        redisStockClient.getStock(productId) shouldBe 70 // available - reserved - confirmed
    }

    @Test
    fun `재고 예약 시 판매 가능 재고가 부족하면 상품 ID를 반환한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 10)

        // when
        val result = redisStockClient.reserveStocks(1L, mapOf(productId to 20))

        // then
        result shouldBe productId
        redisStockClient.getReservedStock(productId) shouldBe 0 // 예약되지 않음
    }

    @Test
    fun `재고 확정 시 reserved가 감소하고 confirmed가 증가한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)
        redisStockClient.reserveStocks(1L, mapOf(productId to 30))

        // when
        val failedId = redisStockClient.confirmStocks(1L, mapOf(productId to 30))

        // then
        failedId shouldBe null
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
        redisStockClient.reserveStocks(1L, mapOf(productId to 30))

        // when
        redisStockClient.confirmStocks(1L, mapOf(productId to 30))

        // then
        val dirtyIds = redisStockClient.getDirtyStockSnapshot().keys
        dirtyIds shouldContain productId
    }

    @Test
    fun `예약 해제 시 reserved가 감소한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)
        redisStockClient.reserveStocks(1L, mapOf(productId to 30))

        // when
        val failedId = redisStockClient.releaseStocks(1L, mapOf(productId to 30))

        // then
        failedId shouldBe null
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
        redisStockClient.reserveStocks(1L, mapOf(productId to 30))

        // when
        redisStockClient.releaseStocks(1L, mapOf(productId to 30))

        // then
        val dirtyIds = redisStockClient.getDirtyStockSnapshot().keys
        dirtyIds.size shouldBe 0
    }

    @Test
    fun `예약 수량보다 많은 확정은 충돌로 거절한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)
        redisStockClient.reserveStocks(1L, mapOf(productId to 10))

        // when - reserved보다 많이 확정 시도
        shouldThrow<StockReservationConflictException> {
            redisStockClient.confirmStocks(1L, mapOf(productId to 20))
        }

        // then
        redisStockClient.getReservedStock(productId) shouldBe 10 // 변경되지 않음
        redisStockClient.getConfirmedStock(productId) shouldBe 0 // 변경되지 않음
    }

    @Test
    fun `예약 수량보다 많은 해제는 충돌로 거절한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)
        redisStockClient.reserveStocks(1L, mapOf(productId to 10))

        // when - reserved보다 많이 해제 시도
        shouldThrow<StockReservationConflictException> {
            redisStockClient.releaseStocks(1L, mapOf(productId to 20))
        }

        // then
        redisStockClient.getReservedStock(productId) shouldBe 10 // 변경되지 않음
    }

    @Test
    fun `3단계 재고 관리 - 전체 플로우가 정상 동작한다`() {
        // given
        val productId = 1L
        val initialStock = 100
        redisStockClient.setStock(productId, initialStock)

        // when - 1. 주문 생성 (예약)
        redisStockClient.reserveStocks(1L, mapOf(productId to 30))

        // then - 1
        redisStockClient.getStock(productId) shouldBe 70 // 100 - 30 - 0

        // when - 2. 결제 성공 (확정)
        redisStockClient.confirmStocks(1L, mapOf(productId to 30))

        // then - 2
        redisStockClient.getStock(productId) shouldBe 70 // 100 - 0 - 30
        redisStockClient.getReservedStock(productId) shouldBe 0
        redisStockClient.getConfirmedStock(productId) shouldBe 30

        // when - 3. 다른 주문 생성 후 결제 실패 (예약 해제)
        redisStockClient.reserveStocks(2L, mapOf(productId to 20))
        redisStockClient.releaseStocks(2L, mapOf(productId to 20))

        // then - 3
        redisStockClient.getStock(productId) shouldBe 70 // 100 - 0 - 30 (변화 없음)
        redisStockClient.getReservedStock(productId) shouldBe 0
    }

    @Test
    fun `서로 다른 주문 100개의 동시 예약은 재고를 초과하지 않는다`() {
        val productId = 1L
        redisStockClient.setStock(productId, 100)

        runConcurrentOrders { orderId ->
            redisStockClient.reserveStocks(orderId, mapOf(productId to 1)) shouldBe null
        }

        redisStockClient.getStock(productId) shouldBe 0
        redisStockClient.getReservedStock(productId) shouldBe 100
    }

    @Test
    fun `서로 다른 주문의 동시 예약과 확정은 정합성을 유지한다`() {
        val productId = 1L
        redisStockClient.setStock(productId, 100)

        runConcurrentOrders { orderId ->
            redisStockClient.reserveStocks(orderId, mapOf(productId to 1)) shouldBe null
            redisStockClient.confirmStocks(orderId, mapOf(productId to 1)) shouldBe null
        }

        redisStockClient.getStock(productId) shouldBe 0
        redisStockClient.getReservedStock(productId) shouldBe 0
        redisStockClient.getConfirmedStock(productId) shouldBe 100
    }

    private fun runConcurrentOrders(action: (Long) -> Unit) {
        val executor = Executors.newFixedThreadPool(32)
        try {
            val futures = (1L..100L).map { orderId -> executor.submit { action(orderId) } }
            futures.forEach { it.get(10, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun `여러 상품의 재고를 확정하면 모든 상품이 더티 플래그에 추가된다`() {
        // given
        val productIds = listOf(1L, 2L, 3L)
        productIds.forEach { productId ->
            redisStockClient.setStock(productId, 100)
            redisStockClient.reserveStocks(productId, mapOf(productId to 10))
        }

        // when
        productIds.forEach { productId ->
            redisStockClient.confirmStocks(productId, mapOf(productId to 10))
        }

        // then
        val dirtyIds = redisStockClient.getDirtyStockSnapshot().keys
        dirtyIds.size shouldBe productIds.size
        productIds.forEach { productId ->
            dirtyIds shouldContain productId
        }
    }

    @Test
    fun `스냅샷 조회는 변경 표시를 유지하고 저장 확인 후에만 제거한다`() {
        // given
        val productId = 1L
        redisStockClient.setStock(productId, 100)
        redisStockClient.reserveStocks(1L, mapOf(productId to 10))
        redisStockClient.confirmStocks(1L, mapOf(productId to 10))

        // when
        val snapshot = redisStockClient.getDirtyStockSnapshot()

        // then
        snapshot.keys shouldContain productId
        redisStockClient.getDirtyStockSnapshot() shouldBe snapshot
        redisStockClient.acknowledgeStockSync(snapshot)
        val afterClear = redisStockClient.getDirtyStockSnapshot()
        afterClear.size shouldBe 0
    }

    @Test
    fun `dirty set이 비어있으면 빈 스냅샷을 반환한다`() {
        // given - dirty set에 아무것도 없는 상태

        // when
        val dirtyIds = redisStockClient.getDirtyStockSnapshot().keys

        // then
        dirtyIds.size shouldBe 0
    }
}
