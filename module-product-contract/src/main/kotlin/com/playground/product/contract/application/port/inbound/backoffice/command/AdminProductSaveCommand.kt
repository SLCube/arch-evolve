package com.playground.product.contract.application.port.inbound.backoffice.command

import java.math.BigDecimal

data class AdminProductSaveCommand(
    val name: String,
    val stock: Int,
    val price: BigDecimal,
)
