package com.playground.product.application.port.inbound.command

data class StockConfirmCommand(
    val productId: Long,
    val quantity: Int,
)
