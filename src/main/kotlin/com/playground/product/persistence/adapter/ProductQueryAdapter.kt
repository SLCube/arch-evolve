package com.playground.product.persistence.adapter

import com.playground.product.application.port.out.ProductQueryPort
import com.playground.product.domain.Product
import com.playground.product.domain.exception.ProductNotFoundException
import com.playground.product.persistence.mapper.toDomain
import com.playground.product.persistence.repository.ProductRepository
import org.springframework.stereotype.Component

@Component
class ProductQueryAdapter(
    private val productRepository: ProductRepository
): ProductQueryPort {
    override fun findById(id: Long): Product {
        return productRepository.findById(id)
            .orElseThrow { ProductNotFoundException(id) }
            .toDomain()
    }

    override fun findAll(): List<Product> {
        return productRepository.findAll().map { it.toDomain() }
    }

    override fun findAllByIds(productIds: List<Long>): List<Product> {
        return productRepository.findAllById(productIds).map { it.toDomain() }
    }

    override fun findByIdWithPessimisticLock(id: Long): Product {
        return productRepository.findByIdWithPessimisticLock(id)
            .orElseThrow { ProductNotFoundException(id) }
            .toDomain()
    }
}