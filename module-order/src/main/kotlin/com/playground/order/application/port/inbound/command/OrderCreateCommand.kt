package com.playground.order.application.port.inbound.command

data class OrderCreateCommand(
    val userId: Long,
    val orderProducts: List<com.playground.order.application.port.inbound.command.OrderProductCreateCommand>,
)
