package com.playground.product.application.port.outbound

import com.playground.common.application.query.PageQuery
import com.playground.common.application.query.PagedResult
import com.playground.product.domain.model.Product

interface ProductQueryPort {
    fun findById(id: Long): Product

    fun findAll(): List<Product>

    fun findAll(pageQuery: PageQuery): PagedResult<Product>

    fun findAllByIds(productIds: List<Long>): List<Product>
}
