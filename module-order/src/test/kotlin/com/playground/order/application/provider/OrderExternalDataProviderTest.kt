package com.playground.order.application.provider

import com.playground.order.domain.exception.OrderableProductNotFoundException
import com.playground.order.fixture.application.domain.AddressInfoTestFixture
import com.playground.order.fixture.application.domain.ProductInfoTestFixture
import com.playground.product.contract.application.outbound.ProductInfoQueryPort
import com.playground.user.contract.application.port.outbound.AddressInfoQueryPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@Suppress("NonAsciiCharacters")
class OrderExternalDataProviderTest {

    private val productInfoQueryPort: ProductInfoQueryPort = mock()
    private val addressInfoQueryPort: AddressInfoQueryPort = mock()

    private val orderExternalDataProvider: OrderExternalDataProvider = OrderExternalDataProvider(
        productInfoQueryPort = productInfoQueryPort,
        addressInfoQueryPort = addressInfoQueryPort,
    )

    @Test
    fun `상품 목록 ID로 조회 시 정상적으로 상품 정보를 반환해야 된다`() {
        // given
        val productIds = listOf(1L, 2L)
        val mockProductInfos = ProductInfoTestFixture.mockProductInfos()

        given(productInfoQueryPort.getProductInfos(eq(productIds)))
            .willReturn(mockProductInfos)

        // when
        val productInfos = orderExternalDataProvider.getVerifiedProductInfos(productIds)

        // then
        productInfos shouldBe mockProductInfos

        verify(productInfoQueryPort).getProductInfos(eq(productIds))
    }

    @Test
    fun `요청 상품 ID중 일부가 존재하지 않는 상품이라면 OrderableProductNotFoundException를 던져야 한다`() {
        // given
        val nonExistingProductId = 999L
        val productIds = listOf(1L, nonExistingProductId)
        val mockProductInfo = ProductInfoTestFixture.mockProductInfo()
        val mockProductInfoMap = mapOf(
            mockProductInfo.productId to mockProductInfo
        )


        given(productInfoQueryPort.getProductInfos(eq(productIds)))
            .willReturn(mockProductInfoMap)

        // when & then
        val exception = shouldThrow<OrderableProductNotFoundException> {
            orderExternalDataProvider.getVerifiedProductInfos(productIds)
        }

        exception.message shouldContain nonExistingProductId.toString()

        verify(productInfoQueryPort).getProductInfos(eq(productIds))
    }

    @Test
    fun `비어있는 상품 ID으로 호출 시 비어있는 Map을 호출하고 외부 호출은 하지 않아야 한다`() {
        // given
        val productIds = emptyList<Long>()

        // when
        val productInfos = orderExternalDataProvider.getVerifiedProductInfos(productIds)

        // then
        productInfos shouldBe emptyMap()

        verify(productInfoQueryPort, never()).getProductInfos(eq(productIds))
    }

    @Test
    fun `getAddressInfoByAddressId 호출 시 포트를 통해 조회된 주소 정보를 반환해야 한다`() {
        // Given
        val userId = 5L
        val addressId = 1L
        val mockAddressInfo = AddressInfoTestFixture.mockAddressInfo()

        given(addressInfoQueryPort.getAddressInfoByAddressId(eq(userId), eq(addressId))).willReturn(mockAddressInfo)

        // When
        val result = orderExternalDataProvider.getAddressInfoByAddressId(userId, addressId)

        // Then
        result shouldBe mockAddressInfo

        verify(addressInfoQueryPort).getAddressInfoByAddressId(userId, addressId)
    }
}