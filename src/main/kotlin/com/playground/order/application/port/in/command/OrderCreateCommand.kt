package com.playground.order.application.port.`in`.command

data class OrderCreateCommand(
    val userId: Long,
    val orderProducts: List<OrderProductCreateCommand>,
)
