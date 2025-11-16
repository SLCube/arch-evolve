package com.playground.product.application.port.outbound

import com.playground.product.domain.model.Product

interface ProductQueryPort {
    fun findById(id: Long): Product

    fun findAll(): List<Product>

    fun findAllByIds(productIds: List<Long>): List<Product>

    fun findByIdWithPessimisticLock(id: Long): Product
}
