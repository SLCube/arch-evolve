package com.playground.product.service

import com.playground.product.domain.Product
import com.playground.product.repository.ProductRepository
import org.springframework.stereotype.Service

@Service
class ProductService (
    private val productRepository: ProductRepository
) {
    fun save(name: String, stock: Int) : Product {
        val product = Product(name = name, stock = stock)
        return productRepository.save(product)
    }
}