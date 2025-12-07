package com.playground.order.fixture.application.domain

import com.playground.common.application.query.PagedResult
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.model.Order
import com.playground.order.domain.model.OrderAddress
import com.playground.order.domain.model.OrderProduct
import com.playground.order.domain.model.OrderReceiver
import java.time.LocalDateTime

object OrderDomainTestFixture {
    private val DEFAULT_CREATED_AT = LocalDateTime.of(2025, 12, 1, 10, 0, 0)

    fun mockOrder(
        id: Long? = 1L,
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

    fun mockOrdersPage(
        userId: Long = 2L,
        startId: Long = 1L,
        pageNumber: Int = 0,
        pageSize: Int = 10,
    ): PagedResult<Order> {
        val mockOrders = mockOrders(userId = userId, startId = startId)
        return PagedResult(
            content = mockOrders,
            pageNumber = pageNumber,
            pageSize = pageSize,
            totalElements = mockOrders.size.toLong(),
            totalPages = mockOrders.size / pageSize + 1
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

    fun mockOrderProducts(): MutableList<OrderProduct> = mutableListOf(
        OrderProduct(id = 1L, productId = 1L, quantity = 2, price = 5000.toBigDecimal()),
        OrderProduct(id = 2L, productId = 2L, quantity = 3, price = 3000.toBigDecimal()),
    )

    fun mockUnsavedOrderProducts(): MutableList<OrderProduct> = mutableListOf(
        OrderProduct(productId = 1L, quantity = 2, price = 5000.toBigDecimal()),
        OrderProduct(productId = 2L, quantity = 3, price = 3000.toBigDecimal()),
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
}