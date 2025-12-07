package com.playground.product.persistence.adapter

import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.fixture.application.domain.ProductDomainTestFixture
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@Suppress("NonAsciiCharacters")
class ProductInfoQueryAdapterTest {
    private val productQueryPort: ProductQueryPort = mock()

    private val productInfoQueryAdapter = ProductInfoQueryAdapter(productQueryPort)

    @Test
    fun `getProductInfos 호출 시 Product 도메인 모델을 ProductInfo 맵으로 올바르게 매핑해야 한다`() {
        // Given
        val productIds = listOf(1L, 2L)

        val mockProducts = ProductDomainTestFixture.mockProducts(count = 2)

        given(productQueryPort.findAllByIds(eq(productIds)))
            .willReturn(mockProducts)

        // When
        val productInfoMap = productInfoQueryAdapter.getProductInfos(productIds)

        // Then
        verify(productQueryPort).findAllByIds(eq(productIds))

        productInfoMap shouldHaveSize 2

        val p1Info = productInfoMap[1L]
        val p1Original = mockProducts.first()

        p1Info shouldNotBe null
        p1Info?.productId shouldBe p1Original.id
        p1Info?.price shouldBe p1Original.price
        p1Info?.productName shouldBe p1Original.name
    }

    @Test
    fun `빈 상품 ID 목록으로 호출 시 빈 맵을 반환하고 Port를 호출하지 않아야 한다`() {
        // Given
        val productIds = emptyList<Long>()

        // When
        val productInfoMap = productInfoQueryAdapter.getProductInfos(productIds)

        // Then
        productInfoMap.shouldBeEmpty()

        verify(productQueryPort, never()).findAllByIds(any())
    }
}