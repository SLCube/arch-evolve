package com.playground.product.persistence.adapter

import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.domain.exception.ProductNotFoundException
import com.playground.product.domain.model.Product
import com.playground.product.persistence.mapper.toDomain
import com.playground.product.persistence.repository.ProductRepository
import org.springframework.stereotype.Component

@Component
class ProductQueryAdapter(
    private val productRepository: ProductRepository,
) : ProductQueryPort {
    override fun findById(id: Long): Product =
        productRepository
            .findById(id)
            .orElseThrow { ProductNotFoundException(id) }
            .toDomain()

    override fun findAll(): List<Product> = productRepository.findAll().map { it.toDomain() }

    override fun findAllByIds(productIds: List<Long>): List<Product> = productRepository.findAllById(productIds).map { it.toDomain() }
}
