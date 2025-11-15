package com.playground.product.application.port.`in`.command

data class DecreaseStockCommand(
    val id: Long,
    val quantity: Int,
)
