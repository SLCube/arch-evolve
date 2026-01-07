package com.playground.product.contract.application.port.outbound.backoffice.command

import java.math.BigDecimal

data class AdminProductSaveCommand(
    val name: String,
    val stock: Int,
    val price: BigDecimal,
)
