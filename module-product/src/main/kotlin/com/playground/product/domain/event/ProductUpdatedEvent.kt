package com.playground.product.domain.event

import com.playground.common.event.DomainEvent
import java.math.BigDecimal

data class ProductUpdatedEvent(
    val productId: Long,
    val oldName: String,
    val newName: String,
    val oldStock: Int,
    val newStock: Int,
    val oldPrice: BigDecimal,
    val newPrice: BigDecimal,
) : DomainEvent
