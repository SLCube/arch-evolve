package com.playground.product.domain.event

import com.playground.common.event.DomainEvent

data class ProductStockDecreasedEvent(
    val productId: Long,
    val productName: String,
    val oldStock: Int,
    val decreasedQuantity: Int,
    val newStock: Int,
) : DomainEvent
