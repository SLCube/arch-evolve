package com.playground.order.fixture.application.query

import com.playground.common.application.query.PagedResult
import com.playground.order.application.service.result.OrderSummaryResult
import com.playground.order.domain.model.Order
import com.playground.order.fixture.application.domain.OrderDomainTestFixture
import com.playground.order.fixture.application.domain.ProductInfoTestFixture

object OrderQueryTestFixture {
    fun mockOrderSummaryPage(
        pageNumber: Int = 0,
        pageSize: Int = 10,
        orders: List<Order> = OrderDomainTestFixture.mockOrders(),
    ): PagedResult<OrderSummaryResult> {
        val mockProductInfo = ProductInfoTestFixture.mockProductInfos()
        val mockOrdersSummary = orders.map { order ->
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
}