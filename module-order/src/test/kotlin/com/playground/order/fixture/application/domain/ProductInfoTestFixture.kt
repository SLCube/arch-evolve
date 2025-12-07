package com.playground.order.fixture.application.domain

import com.playground.product.contract.domain.vo.ProductInfo

object ProductInfoTestFixture {
    fun mockProductInfos(): Map<Long, ProductInfo> {
        return listOf(
            ProductInfo(productId = 1L, price = 5000.toBigDecimal(), productName = "상품1"),
            ProductInfo(productId = 2L, price = 3000.toBigDecimal(), productName = "상품2"),
        ).associateBy(
            keySelector = { it.productId },
            valueTransform = { it }
        )
    }

    fun mockProductInfo(
        productId: Long = 1L,
        price: Int = 5000,
        productName: String = "상품1",
    ) = ProductInfo(
        productId = productId,
        price = price.toBigDecimal(),
        productName = productName,
    )
}