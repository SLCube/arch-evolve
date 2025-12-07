package com.playground.order.fixture.presentation.request

import com.playground.order.presentation.request.OrderCreateRequestDto
import com.playground.order.presentation.request.OrderProductRequestDto

object OrderRequestTestFixture {
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
}