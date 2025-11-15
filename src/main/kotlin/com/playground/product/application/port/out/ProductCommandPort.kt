package com.playground.product.application.port.out

import com.playground.product.domain.Product

interface ProductCommandPort {
    fun save(product: Product): Product

    fun update(product: Product): Product
}
