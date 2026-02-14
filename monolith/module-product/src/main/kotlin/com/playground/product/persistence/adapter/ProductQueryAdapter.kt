package com.playground.product.persistence.adapter

import com.playground.common.application.query.PageQuery
import com.playground.common.application.query.PagedResult
import com.playground.common.application.query.toSort
import com.playground.product.application.port.outbound.ProductQueryPort
import com.playground.product.domain.exception.ProductNotFoundException
import com.playground.product.domain.model.Product
import com.playground.product.persistence.mapper.toDomain
import com.playground.product.persistence.repository.ProductRepository
import org.springframework.data.domain.PageRequest
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

    override fun findAll(pageQuery: PageQuery): PagedResult<Product> {
        val sort = pageQuery.toSort()
        val pageable = PageRequest.of(pageQuery.pageNumber, pageQuery.pageSize, sort)
        val page = productRepository.findAll(pageable)

        return PagedResult(
            content = page.content.map { it.toDomain() },
            pageNumber = page.number,
            pageSize = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
        )
    }

    override fun findAllByIds(productIds: List<Long>): List<Product> = productRepository.findAllById(productIds).map { it.toDomain() }
}
