package com.playground.product.persistence.mapper

import com.playground.product.domain.Product
import com.playground.product.persistence.entity.ProductJpaEntity

fun ProductJpaEntity.toDomain(): Product {
    return Product(
        id = this.id,
        name = this.name,
        stock = this.stock,
        price = this.price
    )
}

