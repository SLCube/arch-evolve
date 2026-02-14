package com.playground.product.application.port.inbound

import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.application.port.inbound.command.StockConfirmCommand
import com.playground.product.application.port.inbound.command.StockReleaseCommand

interface StockUseCase {
    fun decreaseStocks(commands: List<DecreaseStockCommand>)

    fun confirmStocks(commands: List<StockConfirmCommand>)

    fun releaseReservedStocks(commands: List<StockReleaseCommand>)
}
