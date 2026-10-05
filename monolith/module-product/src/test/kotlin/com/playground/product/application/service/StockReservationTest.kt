package com.playground.product.application.service

import com.playground.common.error.BusinessException
import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.application.port.inbound.command.StockConfirmCommand
import com.playground.product.application.port.inbound.command.StockReleaseCommand
import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.domain.exception.InsufficientStockException
import com.playground.product.fixture.application.domain.ProductDomainTestFixture
import com.playground.product.infra.redis.client.RedisStockClient
import com.playground.support.IntegrationTestSupport
import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@Suppress("NonAsciiCharacters")
@SpringBootTest
class StockReservationTest(
    @param:Autowired private val stockClient: RedisStockClient,
    @param:Autowired private val redisTemplate: RedisTemplate<String, String>,
) : IntegrationTestSupport() {
    private val productQueryPort: ProductQueryPort = mock()
    private val service by lazy { StockService(productQueryPort, stockClient) }
    private val orderId = 123L
    private val reservationKey = "product:stock:reservation:$orderId"
    private val commands = listOf(DecreaseStockCommand(11L, 10), DecreaseStockCommand(22L, 20))

    @BeforeEach
    fun setUp() {
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
        listOf(11L, 22L).forEach { productId ->
            given(productQueryPort.findById(productId)).willReturn(
                ProductDomainTestFixture.mockProduct(id = productId, stock = 100),
            )
            stockClient.setStock(productId, 100)
        }
    }

    @AfterEach
    fun tearDown() {
        redisTemplate.connectionFactory?.connection?.serverCommands()?.flushAll()
    }

    @Test
    fun `중복 확정은 한 번만 반영하고 다른 주문의 예약을 유지한다`() {
        service.decreaseStocks(orderId, commands)
        service.decreaseStocks(orderId + 1, commands)
        repeat(2) {
            service.confirmStocks(orderId, commands.map { StockConfirmCommand(it.id, it.quantity) })
        }
        stockClient.getReservedStock(11L) shouldBe 10
        stockClient.getConfirmedStock(11L) shouldBe 10
        stockClient.getReservedStock(22L) shouldBe 20
        stockClient.getConfirmedStock(22L) shouldBe 20
        redisTemplate.opsForHash<String, String>().get(reservationKey, "status") shouldBe "CONFIRMED"
    }

    @Test
    fun `중복 해제는 한 번만 반영하고 다른 주문의 예약을 유지한다`() {
        service.decreaseStocks(orderId, commands)
        service.decreaseStocks(orderId + 1, commands)
        repeat(2) {
            service.releaseReservedStocks(orderId, commands.map { StockReleaseCommand(it.id, it.quantity) })
        }
        stockClient.getReservedStock(11L) shouldBe 10
        stockClient.getReservedStock(22L) shouldBe 20
        redisTemplate.opsForHash<String, String>().get(reservationKey, "status") shouldBe "RELEASED"
    }

    @Test
    fun `해제된 주문은 다시 예약하거나 확정할 수 없다`() {
        service.decreaseStocks(orderId, commands)
        service.releaseReservedStocks(orderId, commands.map { StockReleaseCommand(it.id, it.quantity) })
        shouldThrow<BusinessException> { service.decreaseStocks(orderId, commands) }
        shouldThrow<BusinessException> {
            service.confirmStocks(orderId, commands.map { StockConfirmCommand(it.id, it.quantity) })
        }
        stockClient.getReservedStock(11L) shouldBe 0
        stockClient.getConfirmedStock(11L) shouldBe 0
    }

    @Test
    fun `확정된 주문의 예약 해제는 거절한다`() {
        service.decreaseStocks(orderId, commands)
        service.confirmStocks(orderId, commands.map { StockConfirmCommand(it.id, it.quantity) })
        shouldThrow<BusinessException> {
            service.releaseReservedStocks(orderId, commands.map { StockReleaseCommand(it.id, it.quantity) })
        }
        stockClient.getConfirmedStock(11L) shouldBe 10
    }

    @Test
    fun `예약과 다른 수량의 확정은 어느 상품에도 반영하지 않는다`() {
        service.decreaseStocks(orderId, commands)
        shouldThrow<BusinessException> {
            service.confirmStocks(orderId, listOf(StockConfirmCommand(11L, 10), StockConfirmCommand(22L, 21)))
        }
        stockClient.getReservedStock(11L) shouldBe 10
        stockClient.getConfirmedStock(11L) shouldBe 0
    }

    @Test
    fun `예약 전 해제가 도착해도 다른 주문을 건드리지 않고 늦은 예약을 막는다`() {
        service.decreaseStocks(orderId + 1, commands)
        service.releaseReservedStocks(orderId, commands.map { StockReleaseCommand(it.id, it.quantity) })
        shouldThrow<BusinessException> { service.decreaseStocks(orderId, commands) }
        stockClient.getReservedStock(11L) shouldBe 10
        redisTemplate.opsForHash<String, String>().get(reservationKey, "status") shouldBe "RELEASED"
    }

    @Test
    fun `같은 주문을 두 번 예약해도 상품별 수량은 한 번만 증가한다`() {
        repeat(2) { service.decreaseStocks(orderId, commands) }

        assertSoftly {
            stockClient.getReservedStock(11L) shouldBe 10
            stockClient.getReservedStock(22L) shouldBe 20
            stockClient.getStock(11L) shouldBe 90
            stockClient.getStock(22L) shouldBe 80
        }
    }

    @Test
    fun `예약 성공 시 주문별 상품 수량과 RESERVED 상태를 기록한다`() {
        service.decreaseStocks(orderId, commands)

        redisTemplate.opsForHash<String, String>().entries(reservationKey) shouldBe
            mapOf("status" to "RESERVED", "items" to "11:10;22:20")
    }

    @Test
    fun `같은 주문에 다른 수량으로 예약하면 충돌로 거절하고 기존 예약을 유지한다`() {
        service.decreaseStocks(orderId, commands)

        val exception = shouldThrow<BusinessException> {
            service.decreaseStocks(orderId, listOf(DecreaseStockCommand(11L, 30), DecreaseStockCommand(22L, 20)))
        }

        exception.errorCode.code shouldBe "STOCK_RESERVATION_CONFLICT"
        stockClient.getReservedStock(11L) shouldBe 10
        stockClient.getReservedStock(22L) shouldBe 20
    }

    @Test
    fun `같은 주문에 다른 상품으로 예약하면 충돌로 거절한다`() {
        service.decreaseStocks(orderId, listOf(DecreaseStockCommand(11L, 10)))

        val exception = shouldThrow<BusinessException> {
            service.decreaseStocks(orderId, listOf(DecreaseStockCommand(22L, 10)))
        }

        exception.errorCode.code shouldBe "STOCK_RESERVATION_CONFLICT"
        stockClient.getReservedStock(11L) shouldBe 10
        stockClient.getReservedStock(22L) shouldBe 0
    }

    @Test
    fun `상품 순서와 중복 상품의 분할이 달라도 합산 수량이 같으면 같은 예약이다`() {
        service.decreaseStocks(orderId, commands)
        service.decreaseStocks(
            orderId,
            listOf(DecreaseStockCommand(22L, 20), DecreaseStockCommand(11L, 4), DecreaseStockCommand(11L, 6)),
        )

        stockClient.getReservedStock(11L) shouldBe 10
        stockClient.getReservedStock(22L) shouldBe 20
    }

    @Test
    fun `재고 부족 시 예약 기록을 남기지 않고 같은 주문으로 다시 시도할 수 있다`() {
        stockClient.setStock(22L, 5)
        shouldThrow<InsufficientStockException> { service.decreaseStocks(orderId, commands) }

        redisTemplate.hasKey(reservationKey) shouldBe false
        stockClient.getReservedStock(11L) shouldBe 0
        stockClient.getReservedStock(22L) shouldBe 0

        stockClient.setStock(22L, 100)
        service.decreaseStocks(orderId, commands)
        stockClient.getReservedStock(11L) shouldBe 10
        stockClient.getReservedStock(22L) shouldBe 20
    }

    @Test
    fun `다른 주문의 동일 상품 예약은 각각 반영한다`() {
        service.decreaseStocks(orderId, commands)
        service.decreaseStocks(orderId + 1, commands)

        stockClient.getReservedStock(11L) shouldBe 20
        stockClient.getReservedStock(22L) shouldBe 40
    }

    @Test
    fun `같은 주문의 동시 예약 요청도 한 번만 반영한다`() {
        val executor = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)
        try {
            val futures =
                (1..20).map {
                    executor.submit {
                        check(start.await(10, TimeUnit.SECONDS))
                        service.decreaseStocks(orderId, commands)
                    }
                }
            start.countDown()
            futures.forEach { it.get(10, TimeUnit.SECONDS) }

            stockClient.getReservedStock(11L) shouldBe 10
            stockClient.getReservedStock(22L) shouldBe 20
        } finally {
            executor.shutdownNow()
        }
    }
}
