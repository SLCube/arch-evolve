package com.playground.product.application.service

import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.application.port.outbound.StockCachePort
import com.playground.product.application.service.customer.StockService
import com.playground.product.domain.exception.InsufficientStockException
import com.playground.product.fixture.application.command.ProductCommandTestFixture
import com.playground.product.fixture.application.command.StockCommandTestFixture
import com.playground.product.fixture.application.domain.ProductDomainTestFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@Suppress("NonAsciiCharacters")
class StockServiceTest {
    private val productQueryPort: ProductQueryPort = mock()
    private val stockCachePort: StockCachePort = mock()

    private val stockService =
        StockService(
            productQueryPort = productQueryPort,
            stockCachePort = stockCachePort,
        )

    @Test
    fun `재고 예약 성공 시 reserveStock이 호출되고 productId를 반환해야 한다`() {
        // given
        val productId = 1L
        val initialStock = 100
        val quantityToDecrease = 10
        val command =
            ProductCommandTestFixture.mockDecreaseStockCommand(
                id = productId,
                quantity = quantityToDecrease,
            )

        val mockProduct = ProductDomainTestFixture.mockProduct(id = productId, stock = initialStock)

        given(productQueryPort.findById(productId)).willReturn(mockProduct)
        given(stockCachePort.reserveStock(productId, quantityToDecrease))
            .willReturn(90L)

        // when
        val result = stockService.decreaseStock(command)

        // then
        result shouldBe productId
        verify(productQueryPort).findById(productId)
        verify(stockCachePort).reserveStock(productId, quantityToDecrease)
    }

    @Test
    fun `재고 예약 실패 시 InsufficientStockException을 던져야 한다`() {
        // given
        val productId = 1L
        val initialStock = 10
        val quantityToDecrease = 20
        val command =
            ProductCommandTestFixture.mockDecreaseStockCommand(
                id = productId,
                quantity = quantityToDecrease,
            )

        val mockProduct = ProductDomainTestFixture.mockProduct(id = productId, stock = initialStock)

        given(productQueryPort.findById(productId)).willReturn(mockProduct)
        given(stockCachePort.reserveStock(productId, quantityToDecrease))
            .willReturn(-1L)

        // when & then
        shouldThrow<InsufficientStockException> {
            stockService.decreaseStock(command)
        }
    }

    @Test
    fun `재고 확정 시 confirmStock이 호출되고 confirmed 값을 반환해야 한다`() {
        // given
        val productId = 1L
        val quantity = 10
        val command = StockCommandTestFixture.mockStockConfirmCommand(productId = productId, quantity = quantity)

        given(stockCachePort.confirmStock(productId, quantity))
            .willReturn(10L)

        // when
        val result = stockService.confirmStock(command)

        // then
        result shouldBe 10L
        verify(stockCachePort).confirmStock(productId, quantity)
    }

    @Test
    fun `예약 해제 시 releaseReservedStock이 호출되고 reserved 값을 반환해야 한다`() {
        // given
        val productId = 1L
        val quantity = 10
        val command = StockCommandTestFixture.mockStockReleaseCommand(productId = productId, quantity = quantity)

        given(stockCachePort.releaseReservedStock(productId, quantity))
            .willReturn(0L)

        // when
        val result = stockService.releaseReservedStock(command)

        // then
        result shouldBe 0L
        verify(stockCachePort).releaseReservedStock(productId, quantity)
    }
}
