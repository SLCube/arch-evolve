package com.playground.order.application.port.inbound.command

data class OrderCreateCommand(
    val userId: Long,
    val addressId: Long,
    val orderProducts: List<OrderProductCreateCommand>,
)
