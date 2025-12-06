package com.playground.order.fixture

import com.playground.order.fixture.OrderTestFixture.defaultOrderProductList
import com.playground.order.presentation.request.OrderCreateRequestDto
import com.playground.order.presentation.request.OrderProductRequestDto

object OrderRequestTestFixture {
    fun createOrderRequest(
        addressId: Long = 1L,
        orderProducts: List<OrderProductRequestDto> = defaultOrderProductList()
    ) = OrderCreateRequestDto(
        addressId = addressId,
        orderProducts = orderProducts
    )
}