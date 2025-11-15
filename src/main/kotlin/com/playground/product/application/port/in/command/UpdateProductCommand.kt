package com.playground.product.application.port.`in`.command

data class UpdateProductCommand(
    val id: Long,
    val name: String,
    val stock: Int,
    val price: Long,
)
