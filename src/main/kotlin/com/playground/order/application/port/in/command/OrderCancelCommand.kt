package com.playground.order.application.port.`in`.command

data class OrderCancelCommand(
    val userId: Long,
    val orderId: Long,
)
