package com.playground.product.persistence.adapter

import com.playground.common.jpa.config.QuerydslConfig
import com.playground.product.domain.exception.ProductNotFoundException
import com.playground.product.fixture.application.domain.ProductDomainTestFixture
import com.playground.product.persistence.repository.ProductRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(ProductCommandAdapter::class, QuerydslConfig::class)
class ProductCommandAdapterTest(
    @param:Autowired private val productCommandAdapter: ProductCommandAdapter,
    @param:Autowired private val productRepository: ProductRepository,
) {

    private val initialPrice = 10000.toBigDecimal()
    private val initialStock = 100
    private val initialName = "테스트 상품"

    @Test
    fun `save 호출 시 Product 도메인 모델을 DB에 저장하고 ID가 할당된 객체를 반환해야 한다`() {
        // Given
        val unsavedProduct = ProductDomainTestFixture.mockProduct(
            id = null,
            stock = initialStock,
            price = initialPrice,
            name = initialName
        )

        // When
        val savedProduct = productCommandAdapter.save(unsavedProduct)

        // Then
        savedProduct.id shouldNotBe null

        val foundEntity = productRepository.findById(savedProduct.id!!).orElse(null)
        foundEntity shouldNotBe null
        foundEntity.stock shouldBe initialStock
        foundEntity.name shouldBe initialName
    }

    @Test
    fun `update 호출 시 Product 도메인 모델의 변경 사항이 DB에 반영되어야 한다`() {
        // Given
        val initialProduct = productCommandAdapter.save(
            ProductDomainTestFixture.mockProduct(id = null, stock = initialStock)
        )
        val productId = initialProduct.id!!

        val newStock = 50
        val newName = "업데이트 상품"

        val updatedDomainProduct = initialProduct.apply {
            update(name = newName, stock = newStock, price = initialPrice)
        }

        // When
        productCommandAdapter.update(updatedDomainProduct)

        // Then
        val foundEntity = productRepository.findById(productId).get()
        foundEntity.stock shouldBe newStock
        foundEntity.name shouldBe newName
    }

    @Test
    fun `update 호출 시 해당 ID의 Product가 DB에 없으면 ProductNotFoundException을 던져야 한다`() {
        // Given
        val nonExistingProductId = 999L
        val domainProduct = ProductDomainTestFixture.mockProduct(id = nonExistingProductId)

        // When & Then
        shouldThrow<ProductNotFoundException> {
            productCommandAdapter.update(domainProduct)
        }
    }
}