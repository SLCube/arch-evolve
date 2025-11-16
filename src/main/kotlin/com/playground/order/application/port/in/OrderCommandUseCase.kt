package com.playground.order.application.port.`in`

import com.playground.order.application.port.`in`.command.OrderCancelCommand
import com.playground.order.application.port.`in`.command.OrderCreateCommand
import com.playground.order.domain.model.Order

interface OrderCommandUseCase {
    fun createOrder(command: OrderCreateCommand): Order

    fun cancelOrder(command: OrderCancelCommand): Order
}
