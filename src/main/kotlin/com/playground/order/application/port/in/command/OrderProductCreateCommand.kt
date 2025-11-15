package com.playground.order.application.port.`in`.command

data class OrderProductCreateCommand(
    val productId: Long,
    val quantity: Int,
)
