package com.playground.product.service

import com.playground.product.domain.Product
import com.playground.product.exception.ProductNotFoundException
import com.playground.product.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository
) {
    fun save(name: String, stock: Int): Product {
        val product = Product(name = name, stock = stock)
        return productRepository.save(product)
    }

    fun findById(id: Long): Product = productRepository.findById(id).orElseThrow { ProductNotFoundException(id) }

    fun findAll(): List<Product> = productRepository.findAll()

    fun update(id: Long, name: String, stock: Int): Product {
        val foundProduct = productRepository.findById(id).orElseThrow { ProductNotFoundException(id) }
        foundProduct.update(name, stock)

        return foundProduct
    }
}