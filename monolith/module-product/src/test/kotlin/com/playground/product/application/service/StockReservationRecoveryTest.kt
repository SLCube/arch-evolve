package com.playground.product.application.service

import com.playground.order.contract.port.OrderStockRecoveryPort
import com.playground.order.contract.port.OrderStockOutcome
import com.playground.product.infra.redis.client.RedisStockClient
import com.playground.support.IntegrationTestSupport
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.doReturn
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate

@Suppress("NonAsciiCharacters")
@SpringBootTest
class StockReservationRecoveryTest(
    @param:Autowired private val stock: RedisStockClient,
    @param:Autowired private val redis: RedisTemplate<String, String>,
) : IntegrationTestSupport() {
    private val orders: OrderStockRecoveryPort = mock()
    private val quantities = mapOf(11L to 10)

    @BeforeEach
    fun setUp() {
        redis.connectionFactory?.connection?.serverCommands()?.flushAll()
        stock.setStockBatch(mapOf(11L to 100))
        stock.reserveStocks(123L, quantities)
    }

    @AfterEach
    fun tearDown() {
        redis.connectionFactory?.connection?.serverCommands()?.flushAll()
    }

    @Test
    fun `프로세스 종료로 확정 이벤트가 누락돼도 커밋된 주문을 확인하여 확정한다`() {
        given(orders.inspect(123L)).willReturn(OrderStockOutcome.CONFIRMED)
        StockReservationRecoveryService(stock, orders).recover(0)
        stock.getReservedStock(11L) shouldBe 0
        stock.getConfirmedStock(11L) shouldBe 10
        stock.getReservationsForRecovery(Long.MAX_VALUE, 100) shouldBe emptyList()
    }

    @Test
    fun `DB에서 실패 또는 취소가 확인된 주문의 예약은 해제한다`() {
        given(orders.inspect(123L)).willReturn(OrderStockOutcome.RELEASED)
        StockReservationRecoveryService(stock, orders).recover(0)
        stock.getReservedStock(11L) shouldBe 0
        stock.getConfirmedStock(11L) shouldBe 0
    }

    @Test
    fun `진행 중이거나 결과를 확인할 수 없는 주문은 해제하지 않는다`() {
        given(orders.inspect(123L)).willReturn(OrderStockOutcome.PENDING, OrderStockOutcome.UNKNOWN)
        StockReservationRecoveryService(stock, orders).recover(0)
        StockReservationRecoveryService(stock, orders).recover(30001)
        stock.getReservedStock(11L) shouldBe 10
    }

    @Test
    fun `복구 조회 실패 후 새 인스턴스에서 다시 시도할 수 있다`() {
        given(orders.inspect(123L)).willThrow(IllegalStateException("일시적인 DB 장애"))
        StockReservationRecoveryService(stock, orders).recover(0)
        stock.getReservedStock(11L) shouldBe 10
        doReturn(OrderStockOutcome.RELEASED).`when`(orders).inspect(123L)
        StockReservationRecoveryService(stock, orders).recover(30001)
        stock.getReservedStock(11L) shouldBe 0
    }

    @Test
    fun `재시작 시 예약 기록으로 복구 인덱스를 재구성한다`() {
        redis.delete("product:stock:reservations:pending")
        stock.rebuildReservationRecoveryIndex()
        given(orders.inspect(123L)).willReturn(OrderStockOutcome.CONFIRMED)
        StockReservationRecoveryService(stock, orders).recover(0)
        stock.getConfirmedStock(11L) shouldBe 10
    }
}
