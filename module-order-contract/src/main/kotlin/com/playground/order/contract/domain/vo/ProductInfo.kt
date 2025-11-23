package com.playground.order.contract.domain.vo

import java.math.BigDecimal

data class ProductInfo(
    val productId: Long,
    val price: BigDecimal,
    val productName: String,
)
