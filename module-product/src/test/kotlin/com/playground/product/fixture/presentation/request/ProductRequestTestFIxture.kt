package com.playground.product.fixture.presentation.request

import com.playground.product.presentation.request.ProductSaveRequestDto
import java.math.BigDecimal

object ProductRequestTestFIxture {

    fun mockProductSaveRequest(
        name: String = "상품1",
        stock: Int = 10,
        price: BigDecimal = 10000.toBigDecimal(),
    ) = ProductSaveRequestDto(
        name = name,
        stock = stock,
        price = price,
    )
}