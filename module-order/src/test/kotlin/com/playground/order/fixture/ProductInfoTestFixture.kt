package com.playground.order.fixture

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
}