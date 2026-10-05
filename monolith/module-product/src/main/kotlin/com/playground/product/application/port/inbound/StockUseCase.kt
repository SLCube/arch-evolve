package com.playground.product.application.port.inbound

import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.application.port.inbound.command.StockConfirmCommand
import com.playground.product.application.port.inbound.command.StockReleaseCommand

interface StockUseCase {
    fun decreaseStocks(
        orderId: Long,
        commands: List<DecreaseStockCommand>,
    )

    fun confirmStocks(
        orderId: Long,
        commands: List<StockConfirmCommand>,
    )

    fun releaseReservedStocks(
        orderId: Long,
        commands: List<StockReleaseCommand>,
    )
}
