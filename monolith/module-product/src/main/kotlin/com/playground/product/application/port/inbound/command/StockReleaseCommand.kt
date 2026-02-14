package com.playground.product.application.port.inbound.command

data class StockReleaseCommand(
    val productId: Long,
    val quantity: Int,
)
