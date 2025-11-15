package com.playground.product.domain.event

import com.playground.common.event.DomainEvent

data class ProductCreatedEvent(
    val productId: Long,
    val name: String,
    val stock: Int,
    val price: Long
) : DomainEvent