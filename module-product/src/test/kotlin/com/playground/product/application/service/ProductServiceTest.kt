package com.playground.product.application.service

import com.playground.product.application.port.outbound.ProductCommandPort
import com.playground.product.application.port.outbound.ProductEventPort
import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.application.service.customer.ProductService
import com.playground.product.domain.event.ProductCreatedEvent
import com.playground.product.domain.event.ProductStockDecreasedEvent
import com.playground.product.domain.event.ProductUpdatedEvent
import com.playground.product.domain.exception.InsufficientStockException
import com.playground.product.domain.model.Product
import com.playground.product.fixture.application.command.ProductCommandTestFixture
import com.playground.product.fixture.application.domain.ProductDomainTestFixture
import com.playground.product.fixture.application.query.ProductQueryTestFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@Suppress("NonAsciiCharacters")
class ProductServiceTest {

    private val productCommandPort: ProductCommandPort = mock()
    private val productQueryPort: ProductQueryPort = mock()
    private val productEventPort: ProductEventPort = mock()

    private val productService = ProductService(
        productCommandPort = productCommandPort,
        productQueryPort = productQueryPort,
        productEventPort = productEventPort,
    )

    @Test
    fun `상품 저장 Command 수신 시 상품이 저장되고 ProductCreatedEvent가 발행돼야 한다`() {
        // given
        val productId = 1L
        val mockProductSaveCommand = ProductCommandTestFixture.mockProductSaveCommand()

        val mockProduct = ProductDomainTestFixture.mockProduct(
            id = productId,
            name = mockProductSaveCommand.name,
            stock = mockProductSaveCommand.stock,
            price = mockProductSaveCommand.price
        )

        given(productCommandPort.save(any<Product>())).willReturn(mockProduct)

        // when
        val savedProduct = productService.saveProduct(mockProductSaveCommand)

        // then
        savedProduct shouldBe mockProduct

        verify(productCommandPort).save(any<Product>())

        verify(productEventPort).publish(check<ProductCreatedEvent> { event ->
            event.productId shouldBe productId
            event.name shouldBe savedProduct.name
            event.stock shouldBe savedProduct.stock
        })
    }

    @Test
    fun `상품 수정 Command 수신 시 상품 정보가 변경되고 ProductUpdatedEvent가 발행되어야 한다`() {
        // given
        val productId = 10L
        val oldStock = 50
        val newStock = 70
        val newName = "새로운 상품명"

        val initialProduct = ProductDomainTestFixture.mockProduct(
            id = productId,
            name = "오래된 이름",
            stock = oldStock,
        )

        val command = ProductCommandTestFixture.mockProductUpdateCommand(
            id = productId,
            name = newName,
            stock = newStock,
        )

        given(productQueryPort.findById(productId)).willReturn(initialProduct)

        given(productCommandPort.update(any<Product>())).willAnswer { invocation ->
            invocation.arguments[0] as Product
        }

        // when
        val updatedProduct = productService.updateProduct(command)

        // then
        updatedProduct.name shouldBe newName
        updatedProduct.stock shouldBe newStock

        verify(productQueryPort).findById(productId)

        verify(productEventPort).publish(check<ProductUpdatedEvent> { event ->
            event.productId shouldBe productId
            event.oldStock shouldBe oldStock
            event.newStock shouldBe newStock
            event.oldName shouldBe "오래된 이름"
            event.newName shouldBe newName
        })
    }

    @Test
    fun `재고 차감 Command 수신 시 재고가 차감되고 ProductStockDecreasedEvent가 발행돼야 한다`() {
        // given
        val productId = 1L
        val initialStock = 100
        val quantityToDecrease = 10
        val command = ProductCommandTestFixture.mockDecreaseStockCommand(
            id = productId,
            quantity = quantityToDecrease,
        )

        val mockProduct = ProductDomainTestFixture.mockProduct(id = productId, stock = initialStock)

        given(productQueryPort.findByIdWithPessimisticLock(productId)).willReturn(mockProduct)

        given(productCommandPort.update(any<Product>())).willAnswer { invocation ->
                invocation.arguments[0] as Product
            }

        // when
        val updatedProduct = productService.decreaseStock(command)

        // then
        updatedProduct.stock shouldBe (initialStock - quantityToDecrease)

        verify(productQueryPort).findByIdWithPessimisticLock(productId)

        verify(productCommandPort).update(any<Product>())

        verify(productEventPort).publish(check<ProductStockDecreasedEvent> { event ->
            event.productId shouldBe productId
            event.productName shouldBe updatedProduct.name
            event.oldStock shouldBe initialStock
            event.decreasedQuantity shouldBe quantityToDecrease
            event.newStock shouldBe (initialStock - quantityToDecrease)
        })
    }

    @Test
    fun `재고 차감 Command 수신 시 재고가 부족하면 InsufficientStockException을 던져야 한다`() {
        // given
        val productId = 1L
        val initialStock = 10
        val quantityToDecrease = 20
        val command = ProductCommandTestFixture.mockDecreaseStockCommand(
            id = productId,
            quantity = quantityToDecrease,
        )

        val mockProduct = ProductDomainTestFixture.mockProduct(id = productId, stock = initialStock)

        given(productQueryPort.findByIdWithPessimisticLock(productId)).willReturn(mockProduct)

        // when & then
        shouldThrow<InsufficientStockException> {
            productService.decreaseStock(command)
        }

        verify(productCommandPort, never()).update(any())
        verify(productEventPort, never()).publish(any())
    }

    @Test
    fun `단일 상품 조회 시 QueryPort를 통해 상품을 조회하고 반환해야 한다`() {
        // given
        val query = ProductQueryTestFixture.mockProductQuery()
        val mockProduct = ProductDomainTestFixture.mockProduct(id = 1L)

        given(productQueryPort.findById(1L))
            .willReturn(mockProduct)

        // when
        val result = productService.getProduct(query)

        // then
        result shouldBe mockProduct
        verify(productQueryPort).findById(1L)
    }

    @Test
    fun `모든 상품 조회 시 QueryPort를 통해 목록을 조회하고 반환해야 한다`() {
        // given
        val mockProducts = ProductDomainTestFixture.mockProducts(count = 3)

        given(productQueryPort.findAll()).willReturn(mockProducts)

        // when
        val result = productService.getAllProducts()

        // then
        result shouldBe mockProducts
        verify(productQueryPort).findAll()
    }
}