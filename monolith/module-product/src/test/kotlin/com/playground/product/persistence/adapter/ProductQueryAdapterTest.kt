package com.playground.product.persistence.adapter

import com.playground.product.domain.exception.ProductNotFoundException
import com.playground.product.fixture.application.domain.ProductDomainTestFixture
import com.playground.product.persistence.entity.ProductJpaEntity
import com.playground.product.persistence.repository.ProductRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@Suppress("NonAsciiCharacters")
@DataJpaTest
@Import(ProductQueryAdapter::class)
class ProductQueryAdapterTest(
    @param:Autowired private val productQueryAdapter: ProductQueryAdapter,
    @param:Autowired private val productRepository: ProductRepository,
) {

    private val initialPrice = 10000.toBigDecimal()
    private val initialName = "테스트 상품"

    private fun saveTestProduct(name: String = initialName, stock: Int = 100): ProductJpaEntity {
        val domainProduct = ProductDomainTestFixture.mockProduct(
            id = null,
            name = name,
            stock = stock,
            price = initialPrice
        )

        val jpaEntity = ProductJpaEntity.toJpaEntity(domainProduct)
        val savedEntity = productRepository.save(jpaEntity)
        return savedEntity
    }


    @Test
    fun `findById 호출 시 Product 도메인 모델을 반환해야 한다`() {
        // Given
        val savedEntity = saveTestProduct(name = "조회 대상")
        val productId = savedEntity.id!!

        // When
        val product = productQueryAdapter.findById(productId)

        // Then
        product.id shouldBe productId
        product.name shouldBe "조회 대상"
    }

    @Test
    fun `findById 호출 시 해당 ID의 상품이 없으면 ProductNotFoundException을 던져야 한다`() {
        // Given
        val nonExistingId = 999L

        // When & Then
        shouldThrow<ProductNotFoundException> {
            productQueryAdapter.findById(nonExistingId)
        }
    }

    @Test
    fun `findAll 호출 시 DB에 저장된 모든 상품 목록을 반환해야 한다`() {
        // Given
        saveTestProduct("상품 A")
        saveTestProduct("상품 B")
        saveTestProduct("상품 C")

        // When
        val products = productQueryAdapter.findAll()

        // Then
        products shouldHaveSize 3

        products.any { it.name == "상품 B" } shouldBe true
    }

    @Test
    fun `findAllByIds 호출 시 요청 ID에 해당하는 상품 목록만 반환해야 한다`() {
        // Given
        val p1 = saveTestProduct("상품 1")
        val p3 = saveTestProduct("상품 3")

        val requestedIds = listOf(p1.id!!, p3.id!!)

        // When
        val products = productQueryAdapter.findAllByIds(requestedIds)

        // Then
        products shouldHaveSize 2

        products.map { it.id } shouldBe requestedIds
    }
}