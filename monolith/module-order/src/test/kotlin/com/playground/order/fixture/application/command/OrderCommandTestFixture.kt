package com.playground.order.fixture.application.command

import com.playground.order.application.port.inbound.command.OrderCreateCommand
import com.playground.order.application.port.inbound.command.OrderProductCreateCommand

object OrderCommandTestFixture {
    fun defaultOrderProductCommandList(): List<OrderProductCreateCommand> = listOf(
        OrderProductCreateCommand(productId = 1L, quantity = 2),
        OrderProductCreateCommand(productId = 2L, quantity = 3),
    )

    fun createOrderCommand(
        userId: Long = 2L,
        addressId: Long = 1L,
        orderProducts: List<OrderProductCreateCommand> = defaultOrderProductCommandList()
    ) = OrderCreateCommand(
        userId = userId,
        addressId = addressId,
        orderProducts = orderProducts
    )
}