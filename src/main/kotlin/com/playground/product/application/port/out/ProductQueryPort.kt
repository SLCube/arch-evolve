package com.playground.product.application.port.out

import com.playground.product.domain.Product

interface ProductQueryPort {
    fun findById(id: Long): Product
    fun findAll(): List<Product>
    fun findByIdWithPessimisticLock(id: Long): Product
}