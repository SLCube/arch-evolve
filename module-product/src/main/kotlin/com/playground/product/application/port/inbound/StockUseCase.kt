package com.playground.product.application.port.inbound

import com.playground.product.application.port.inbound.command.DecreaseStockCommand
import com.playground.product.application.port.inbound.command.StockConfirmCommand
import com.playground.product.application.port.inbound.command.StockReleaseCommand

interface StockUseCase {
    fun decreaseStock(command: DecreaseStockCommand): Long

    fun confirmStock(command: StockConfirmCommand): Long

    fun releaseReservedStock(command: StockReleaseCommand): Long
}
