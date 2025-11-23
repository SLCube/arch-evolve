package com.playground.order.domain.model

import java.math.BigDecimal

class OrderProduct(
    val id: Long? = null,
    val productId: Long,
    val quantity: Int,
    val price: BigDecimal,
)
