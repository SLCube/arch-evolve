package com.playground.order.application.port.inbound.command

data class OrderCancelCommand(
    val userId: Long,
    val orderId: Long,
)
