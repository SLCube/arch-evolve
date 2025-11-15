package com.playground.product.domain.event

import com.playground.common.event.DomainEvent

data class ProductUpdatedEvent(
    val productId: Long,
    val oldName: String,
    val newName: String,
    val oldStock: Int,
    val newStock: Int,
    val oldPrice: Long,
    val newPrice: Long
) : DomainEvent