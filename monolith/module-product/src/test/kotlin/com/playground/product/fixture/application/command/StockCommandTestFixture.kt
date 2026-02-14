package com.playground.product.fixture.application.command

import com.playground.product.application.port.inbound.command.StockConfirmCommand
import com.playground.product.application.port.inbound.command.StockReleaseCommand

object StockCommandTestFixture {
    fun mockStockConfirmCommand(
        productId: Long = 1L,
        quantity: Int = 1,
    ) = StockConfirmCommand(
        productId = productId,
        quantity = quantity,
    )

    fun mockStockReleaseCommand(
        productId: Long = 1L,
        quantity: Int = 1,
    ) = StockReleaseCommand(
        productId = productId,
        quantity = quantity,
    )
}
