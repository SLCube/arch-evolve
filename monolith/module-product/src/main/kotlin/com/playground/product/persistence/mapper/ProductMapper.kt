package com.playground.product.persistence.mapper

import com.playground.product.domain.model.Product
import com.playground.product.persistence.entity.ProductJpaEntity

fun ProductJpaEntity.toDomain(): Product =
    Product(
        id = this.id,
        name = this.name,
        stock = this.stock,
        price = this.price,
    )
