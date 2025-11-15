package com.playground.product.persistence.adapter

import com.playground.product.application.port.out.ProductCommandPort
import com.playground.product.domain.Product
import com.playground.product.domain.exception.ProductNotFoundException
import com.playground.product.persistence.entity.ProductJpaEntity
import com.playground.product.persistence.mapper.toDomain
import com.playground.product.persistence.repository.ProductRepository
import org.springframework.stereotype.Component

@Component
class ProductCommandAdapter(
    private val productRepository: ProductRepository,
) : ProductCommandPort {
    override fun save(product: Product): Product {
        val productJpaEntity = ProductJpaEntity.toJpaEntity(product)
        val savedEntity = productRepository.save(productJpaEntity)
        return savedEntity.toDomain()
    }

    override fun update(product: Product): Product {
        val productId = requireNotNull(product.id) { "Product ID must not be null for update" }

        val productJpaEntity =
            productRepository
                .findById(productId)
                .orElseThrow { ProductNotFoundException(productId) }

        productJpaEntity.updateFromDomain(product)

        return productJpaEntity.toDomain()
    }
}
