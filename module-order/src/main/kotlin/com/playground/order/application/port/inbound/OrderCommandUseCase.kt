package com.playground.order.application.port.inbound

import com.playground.order.application.port.inbound.command.OrderCancelCommand
import com.playground.order.application.port.inbound.command.OrderCompleteCommand
import com.playground.order.application.port.inbound.command.OrderCreateCommand
import com.playground.order.domain.model.Order

interface OrderCommandUseCase {
    fun createOrder(command: OrderCreateCommand): Order
    fun completeOrder(command: OrderCompleteCommand): Order
    fun cancelOrder(command: OrderCancelCommand): Order
}
