package com.playground.order.application.port.inbound.command

data class OrderProductCreateCommand(
    val productId: Long,
    val quantity: Int,
)
