package com.playground.product.application.port.inbound.command

data class DecreaseStockCommand(
    val id: Long,
    val quantity: Int,
)
