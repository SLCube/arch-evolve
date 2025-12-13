package com.playground.order.application.service

import com.playground.common.application.query.PageQuery
import com.playground.common.application.query.PagedResult
import com.playground.delivery.contract.application.outbound.DeliveryInfoQueryPort
import com.playground.order.application.port.outbound.OrderQueryPort
import com.playground.order.application.provider.OrderExternalDataProvider
import com.playground.order.domain.exception.OrderAccessDeniedException
import com.playground.order.domain.exception.OrderNotFoundException
import com.playground.order.domain.model.Order
import com.playground.order.fixture.application.domain.DeliveryInfoTestFixture
import com.playground.order.fixture.application.domain.OrderDomainTestFixture
import com.playground.order.fixture.application.query.OrderQueryTestFixture
import com.playground.order.fixture.application.domain.ProductInfoTestFixture
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import kotlin.collections.emptyList

@Suppress("NonAsciiCharacters")
class OrderQueryServiceTest {

    private val orderQueryPort: OrderQueryPort = mock()
    private val orderExternalDataProvider: OrderExternalDataProvider = mock()
    private val deliveryInfoQueryPort: DeliveryInfoQueryPort = mock()

    private val orderQueryService: OrderQueryService = OrderQueryService(
        orderQueryPort = orderQueryPort,
        orderExternalDataProvider = orderExternalDataProvider,
        deliveryInfoQueryPort = deliveryInfoQueryPort,
    )

    @Test
    fun `단건 주문 조회 성공 시 OrderDetailsResult를 반환해야 된다`() {
        // given
        val orderId = 1L
        val userId = 2L
        val mockOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            userId = userId,
        )

        val mockProductInfos = ProductInfoTestFixture.mockProductInfos()
        val productIds = mockOrder.orderProducts.map { it.productId }

        given(orderQueryPort.findById(eq(mockOrder.id!!)))
            .willReturn(mockOrder)

        given(orderExternalDataProvider.getVerifiedProductInfos(eq(productIds)))
            .willReturn(mockProductInfos)

        val mockDeliveryInfo = DeliveryInfoTestFixture.mockDeliveryInfo(orderId = orderId, userId = userId)
        given(deliveryInfoQueryPort.getDeliveryInfoByOrderId(eq(orderId)))
            .willReturn(mockDeliveryInfo)

        // when
        val foundOrder = orderQueryService.getOrder(userId, orderId)

        // then
        foundOrder.id shouldBe orderId
        foundOrder.userId shouldBe userId
        foundOrder.orderProducts shouldHaveSize mockOrder.orderProducts.size
        foundOrder.delivery.deliveryId shouldBe 100L
        foundOrder.delivery.deliveryStatus shouldBe "PENDING"

        verify(orderQueryPort).findById(orderId)
        verify(orderExternalDataProvider).getVerifiedProductInfos(eq(productIds))
        verify(deliveryInfoQueryPort).getDeliveryInfoByOrderId(eq(orderId))
    }

    @Test
    fun `존재하지 않는 주문 조회시 OrderNotFoundException을 던져야 한다`() {
        // given
        val nonExistingOrderId = 999L
        val userId = 2L

        given(orderQueryPort.findById(nonExistingOrderId))
            .willThrow(OrderNotFoundException(nonExistingOrderId))

        // when & then
        shouldThrow<OrderNotFoundException> {
            orderQueryService.getOrder(userId, nonExistingOrderId)
        }

        verify(orderQueryPort).findById(nonExistingOrderId)
        verify(orderExternalDataProvider, never()).getVerifiedProductInfos(any())
        verify(deliveryInfoQueryPort, never()).getDeliveryInfoByOrderId(any())
    }

    @Test
    fun `사용자의 주문이 아닌 주문 조회 시도 시 OrderAccessDeniedException을 던져야 한다`() {
        val orderId = 1L
        val ownerUserId = 2L
        val requestUserId = 3L

        val mockOrder = OrderDomainTestFixture.mockOrder(
            id = orderId,
            userId = ownerUserId,
        )

        given(orderQueryPort.findById(eq(mockOrder.id!!)))
            .willReturn(mockOrder)

        shouldThrow<OrderAccessDeniedException> {
            orderQueryService.getOrder(requestUserId, orderId)
        }

        verify(orderQueryPort).findById(orderId)
        verify(orderExternalDataProvider, never()).getVerifiedProductInfos(any())
        verify(deliveryInfoQueryPort, never()).getDeliveryInfoByOrderId(any())
    }

    @Test
    fun `주문 목록 조회 시 OrderSummaryResult가 담긴 PageedResult를 반환해야 한다`() {
        // given
        val userId = 2L
        val pageQuery = PageQuery(
            pageNumber = 0,
            pageSize = 10,
        )

        val mockOrdersPage = OrderDomainTestFixture.mockOrdersPage(
            userId = userId,
            pageNumber = pageQuery.pageNumber,
            pageSize = pageQuery.pageSize,
        )

        val mockOrderSummaryPage = OrderQueryTestFixture.mockOrderSummaryPage(
            orders = mockOrdersPage.content
        )

        val mockProductInfos = ProductInfoTestFixture.mockProductInfos()

        given(orderQueryPort.findOrdersByUserId(eq(userId), eq(pageQuery)))
            .willReturn(mockOrdersPage)

        val productIds = mockOrdersPage.content.flatMap { it.orderProducts.map { op -> op.productId } }.distinct()
        given(orderExternalDataProvider.getVerifiedProductInfos(eq(productIds)))
            .willReturn(mockProductInfos)

        // when
        val result = orderQueryService.getOrders(userId, pageQuery)

        // then
        result.content shouldBe mockOrderSummaryPage.content
        result.content shouldHaveSize mockOrderSummaryPage.content.size

        val firstSummaryResult = result.content.first()
        firstSummaryResult.id shouldBe mockOrderSummaryPage.content.first().id
        val firstProductId = mockOrdersPage.content.first().orderProducts.first().productId
        firstSummaryResult.representativeProductName shouldContain mockProductInfos.getValue(firstProductId).productName

        verify(orderQueryPort).findOrdersByUserId(eq(userId), eq(pageQuery))
        verify(orderExternalDataProvider).getVerifiedProductInfos(eq(productIds))
    }


    @Test
    fun `사용자의 주문 목록이 아닌 주문 목록 조회 시 비어있는 PageedResult를 반환해야 한다`() {
        // given
        val requestUserId = 3L
        val pageQuery = PageQuery(
            pageNumber = 0,
            pageSize = 10
        )

        val emptyOrders = PagedResult(
            content = emptyList<Order>(),
            pageNumber = pageQuery.pageNumber,
            pageSize = pageQuery.pageSize,
            totalElements = 0,
            totalPages = 0
        )

        given(orderQueryPort.findOrdersByUserId(eq(requestUserId), eq(pageQuery)))
            .willReturn(emptyOrders)

        // when
        val result = orderQueryService.getOrders(requestUserId, pageQuery)

        // then
        result.content shouldHaveSize 0

        verify(orderQueryPort).findOrdersByUserId(eq(requestUserId), eq(pageQuery))
        verify(orderExternalDataProvider, never()).getVerifiedProductInfos(any())
    }
}
