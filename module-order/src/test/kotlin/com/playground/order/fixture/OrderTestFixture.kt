package com.playground.order.fixture

import com.playground.common.application.query.PagedResult
import com.playground.order.application.service.result.OrderSummaryResult
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderAddress
import com.playground.order.domain.model.OrderProduct
import com.playground.order.domain.model.OrderReceiver
import com.playground.order.presentation.request.OrderCreateRequestDto
import com.playground.order.presentation.request.OrderProductRequestDto
import com.playground.product.contract.domain.vo.ProductInfo
import java.time.LocalDateTime

object OrderTestFixture {
    private val DEFAULT_CREATED_AT = LocalDateTime.of(2025, 12, 1, 10, 0, 0)

    fun defaultOrderProductList(): List<OrderProductRequestDto> = listOf(
        OrderProductRequestDto(productId = 1L, quantity = 2),
        OrderProductRequestDto(productId = 2L, quantity = 3),
    )

    fun createOrderRequest(
        addressId: Long = 1L,
        orderProducts: List<OrderProductRequestDto> = defaultOrderProductList()
    ) = OrderCreateRequestDto(
        addressId = addressId,
        orderProducts = orderProducts
    )

    fun mockOrder(
        id: Long = 1L,
        userId: Long = 2L,
        status: OrderStatus = OrderStatus.PENDING,
        orderProducts: MutableList<OrderProduct> = mockOrderProducts(),
        orderAddress: OrderAddress = mockOrderAddress(),
        orderReceiver: OrderReceiver = mockOrderReceiver(),
        createdAt: LocalDateTime = DEFAULT_CREATED_AT
    ): Order {
        val totalPrice = orderProducts.sumOf { it.price.multiply(it.quantity.toBigDecimal()) }
        return Order(
            id = id,
            userId = userId,
            totalPrice = totalPrice,
            status = status,
            orderProducts = orderProducts,
            orderAddress = orderAddress,
            orderReceiver = orderReceiver,
            createdAt = createdAt
        )
    }

    fun mockOrders(
        count: Int = 3,
        userId: Long = 2L,
        startId: Long = 1L,
    ): List<Order> {
        return (0 until count).map { index ->
            mockOrder(
                id = startId + index,
                userId = userId,
                status = OrderStatus.PENDING,
                createdAt = DEFAULT_CREATED_AT
            )
        }
    }

    fun mockOrderSummaryPage(
        pageNumber: Int = 0,
        pageSize: Int = 10,
    ): PagedResult<OrderSummaryResult> {
        val mockOrders = mockOrders()
        val mockProductInfo = mockProductInfo()
        val mockOrdersSummary = mockOrders.map { order ->
            OrderSummaryResult.of(order, mockProductInfo)
        }

        return PagedResult(
            content = mockOrdersSummary,
            pageNumber = pageNumber,
            pageSize = pageSize,
            totalElements = mockOrdersSummary.size.toLong(),
            totalPages = mockOrdersSummary.size / pageSize + 1
        )
    }

    fun mockEmptyOrderSummaryPage(
        pageNumber: Int = 0,
        pageSize: Int = 10,
    ): PagedResult<OrderSummaryResult> {
        return PagedResult(
            content = emptyList(),
            pageNumber = pageNumber,
            pageSize = pageSize,
            totalElements = 0,
            totalPages = 0
        )
    }

    fun mockOrderProducts(): MutableList<OrderProduct> = mutableListOf(
        OrderProduct(id = 1L, productId = 1L, quantity = 2, price = 5000.toBigDecimal()),
        OrderProduct(id = 2L, productId = 2L, quantity = 3, price = 3000.toBigDecimal()),
    )

    fun mockOrderAddress() = OrderAddress(
        zipCode = "12345",
        baseAddress = "서울특별시",
        detailAddress = "강남구 역삼동 123-45",
    )

    fun mockOrderReceiver() = OrderReceiver(
        receiverName = "홍길동",
        receiverPhoneNumber = "010-1234-5678",
    )

    fun mockProductInfo(): Map<Long, ProductInfo> {
        return listOf(
            ProductInfo(productId = 1L, price = 5000.toBigDecimal(), productName = "상품1"),
            ProductInfo(productId = 2L, price = 3000.toBigDecimal(), productName = "상품2"),
        ).associateBy(
            keySelector = { it.productId },
            valueTransform = { it }
        )
    }
}