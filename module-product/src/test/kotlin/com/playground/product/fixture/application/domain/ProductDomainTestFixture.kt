package com.playground.product.fixture.application.domain

import com.playground.product.domain.model.Product
import java.math.BigDecimal

object ProductDomainTestFixture {
    fun mockProduct(
        id: Long? = 1L,
        name: String = "상품1",
        stock: Int = 10,
        price: BigDecimal = 10000.toBigDecimal(),
    ) = Product(
        id = id,
        name = name,
        stock = stock,
        price = price,
    )

    fun mockProducts(
        count: Int = 3
    ): List<Product> {
        return (0 until count).map { index ->
            mockProduct(
                id = index.toLong(),
                name = "상품$index",
                stock = 10 * index,
                price = 10000.toBigDecimal().multiply(index.toBigDecimal()),
            )
        }
    }
}