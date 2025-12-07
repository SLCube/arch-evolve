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
}