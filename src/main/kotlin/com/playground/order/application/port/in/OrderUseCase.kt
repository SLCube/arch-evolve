package com.playground.order.application.port.`in`

import com.playground.order.application.port.`in`.command.OrderCreateCommand
import com.playground.order.domain.model.Order

interface OrderUseCase {
    fun createOrder(command: OrderCreateCommand): Order
}