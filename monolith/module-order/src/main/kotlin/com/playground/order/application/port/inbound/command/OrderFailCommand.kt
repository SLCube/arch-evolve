package com.playground.order.application.port.inbound.command

data class OrderFailCommand(
    val orderId: Long,
)
