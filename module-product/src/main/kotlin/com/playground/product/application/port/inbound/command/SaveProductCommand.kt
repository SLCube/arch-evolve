package com.playground.product.application.port.inbound.command

import java.math.BigDecimal

data class SaveProductCommand(
    val name: String,
    val stock: Int,
    val price: BigDecimal,
)
