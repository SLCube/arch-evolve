package com.playground.product.application.port.inbound.command

import java.math.BigDecimal

data class ProductUpdateCommand(
    val id: Long,
    val name: String,
    val stock: Int,
    val price: BigDecimal,
)
