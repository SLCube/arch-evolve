package com.playground.product.fixture.application.command

import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.application.port.inbound.command.ProductSaveCommand
import java.math.BigDecimal

object ProductCommandTestFixture {

    fun mockProductSaveCommand(
        name: String = "테스트 상품",
        stock: Int = 100,
        price: BigDecimal = 10000.toBigDecimal(),
    ) = ProductSaveCommand(
        name = name,
        stock = stock,
        price = price,
    )

    fun mockDecreaseStockCommand(
        id: Long = 1L,
        quantity: Int = 1,
    ) = DecreaseStockCommand(
        id = id,
        quantity = quantity,
    )
}