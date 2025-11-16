package com.playground.order.domain.model

class OrderProduct(
    val id: Long? = null,
    val productId: Long,
    val quantity: Int,
    val price: Long,
)
