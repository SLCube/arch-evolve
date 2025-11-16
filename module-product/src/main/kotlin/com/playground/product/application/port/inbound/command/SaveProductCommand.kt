package com.playground.product.application.port.inbound.command

data class SaveProductCommand(
    val name: String,
    val stock: Int,
    val price: Long,
)
