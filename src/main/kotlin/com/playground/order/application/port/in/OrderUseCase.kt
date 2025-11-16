package com.playground.order.application.port.`in`

import com.playground.order.application.port.`in`.command.OrderCancelCommand
import com.playground.order.application.port.`in`.command.OrderCreateCommand
import com.playground.order.application.service.result.OrderDetailResult
import com.playground.order.domain.model.Order

interface OrderUseCase {
    fun createOrder(command: OrderCreateCommand): Order

    fun cancelOrder(command: OrderCancelCommand): Order

    fun getOrder(
        userId: Long,
        orderId: Long,
    ): OrderDetailResult
}
