package com.playground.product.application.service

import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.application.port.outbound.StockCachePort
import com.playground.product.domain.exception.InsufficientReservedStockException
import com.playground.product.domain.exception.InsufficientStockException
import com.playground.product.fixture.application.command.ProductCommandTestFixture
import com.playground.product.fixture.application.command.StockCommandTestFixture
import com.playground.product.fixture.application.domain.ProductDomainTestFixture
import io.kotest.assertions.throwables.shouldThrow
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
    fun `재고 예약 성공 시 모든 상품을 한 번에 reserveStocks로 전달한다`() {
        // given
        val productId1 = 1L
        val productId2 = 2L
        val initialStock = 100
        val quantityToDecrease = 10

        val commands =
            listOf(
                ProductCommandTestFixture.mockDecreaseStockCommand(
                    id = productId1,
                    quantity = quantityToDecrease,
                ),
                ProductCommandTestFixture.mockDecreaseStockCommand(
                    id = productId2,
                    quantity = quantityToDecrease,
                ),
            )

        val mockProduct1 = ProductDomainTestFixture.mockProduct(id = productId1, stock = initialStock)
        val mockProduct2 = ProductDomainTestFixture.mockProduct(id = productId2, stock = initialStock)

        given(productQueryPort.findById(productId1)).willReturn(mockProduct1)
        given(productQueryPort.findById(productId2)).willReturn(mockProduct2)
        val quantitiesByProductId = mapOf(productId1 to quantityToDecrease, productId2 to quantityToDecrease)
        given(stockCachePort.reserveStocks(quantitiesByProductId)).willReturn(null)

        // when
        stockService.decreaseStocks(orderId = 123L, commands = commands)

        // then
        verify(productQueryPort).findById(productId1)
        verify(productQueryPort).findById(productId2)
        verify(stockCachePort).reserveStocks(quantitiesByProductId)
    }

    @Test
    fun `재고 예약 실패 시 InsufficientStockException을 던져야 한다`() {
        // given
        val productId = 1L
        val initialStock = 10
        val quantityToDecrease = 20
        val commands =
            listOf(
                ProductCommandTestFixture.mockDecreaseStockCommand(
                    id = productId,
                    quantity = quantityToDecrease,
                ),
            )

        val mockProduct = ProductDomainTestFixture.mockProduct(id = productId, stock = initialStock)

        given(productQueryPort.findById(productId)).willReturn(mockProduct)
        given(stockCachePort.reserveStocks(mapOf(productId to quantityToDecrease)))
            .willReturn(productId)

        // when & then
        shouldThrow<InsufficientStockException> {
            stockService.decreaseStocks(orderId = 123L, commands = commands)
        }
    }

    @Test
    fun `재고 확정 시 모든 상품에 대해 confirmStock이 호출되어야 한다`() {
        // given
        val productId1 = 1L
        val productId2 = 2L
        val quantity = 10

        val commands =
            listOf(
                StockCommandTestFixture.mockStockConfirmCommand(productId = productId1, quantity = quantity),
                StockCommandTestFixture.mockStockConfirmCommand(productId = productId2, quantity = quantity),
            )

        given(stockCachePort.confirmStock(productId1, quantity))
            .willReturn(10L)
        given(stockCachePort.confirmStock(productId2, quantity))
            .willReturn(10L)

        // when
        stockService.confirmStocks(orderId = 123L, commands = commands)

        // then
        verify(stockCachePort).confirmStock(productId1, quantity)
        verify(stockCachePort).confirmStock(productId2, quantity)
    }

    @Test
    fun `예약 해제 시 모든 상품에 대해 releaseReservedStock이 호출되어야 한다`() {
        // given
        val productId1 = 1L
        val productId2 = 2L
        val quantity = 10

        val commands =
            listOf(
                StockCommandTestFixture.mockStockReleaseCommand(productId = productId1, quantity = quantity),
                StockCommandTestFixture.mockStockReleaseCommand(productId = productId2, quantity = quantity),
            )

        given(stockCachePort.releaseReservedStock(productId1, quantity))
            .willReturn(0L)
        given(stockCachePort.releaseReservedStock(productId2, quantity))
            .willReturn(0L)

        // when
        stockService.releaseReservedStocks(orderId = 123L, commands = commands)

        // then
        verify(stockCachePort).releaseReservedStock(productId1, quantity)
        verify(stockCachePort).releaseReservedStock(productId2, quantity)
    }

    @Test
    fun `재고 확정 실패 시 InsufficientReservedStockException을 던져야 한다`() {
        // given
        val productId = 1L
        val quantity = 10
        val commands =
            listOf(
                StockCommandTestFixture.mockStockConfirmCommand(productId = productId, quantity = quantity),
            )

        given(stockCachePort.confirmStock(productId, quantity))
            .willReturn(-1L)

        // when & then
        shouldThrow<InsufficientReservedStockException> {
            stockService.confirmStocks(orderId = 123L, commands = commands)
        }
    }

    @Test
    fun `예약 해제 실패 시 InsufficientReservedStockException을 던져야 한다`() {
        // given
        val productId = 1L
        val quantity = 10
        val commands =
            listOf(
                StockCommandTestFixture.mockStockReleaseCommand(productId = productId, quantity = quantity),
            )

        given(stockCachePort.releaseReservedStock(productId, quantity))
            .willReturn(-1L)

        // when & then
        shouldThrow<InsufficientReservedStockException> {
            stockService.releaseReservedStocks(orderId = 123L, commands = commands)
        }
    }
}
