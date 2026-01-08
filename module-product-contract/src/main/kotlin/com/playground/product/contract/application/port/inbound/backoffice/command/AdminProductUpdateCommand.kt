package com.playground.product.contract.application.port.inbound.backoffice.command

import java.math.BigDecimal

data class AdminProductUpdateCommand(
    val id: Long,
    val name: String,
    val stock: Int,
    val price: BigDecimal,
)
