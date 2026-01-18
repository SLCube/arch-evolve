package com.playground.product.application.port.outbound

import com.playground.product.domain.model.Product

interface ProductCommandPort {
    fun save(product: Product): Product

    fun update(product: Product): Product

    fun decreaseStock(product: Product, quantity: Int): Long
}
