package com.playground.admin.product.service.command

import java.math.BigDecimal

data class AdminProductSaveCommand(
    val name: String,
    val stock: Int,
    val price: BigDecimal,
)
