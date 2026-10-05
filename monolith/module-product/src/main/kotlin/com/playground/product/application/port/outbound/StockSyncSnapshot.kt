package com.playground.product.application.port.outbound

data class StockSyncSnapshot(val stock: Int, val confirmed: Int)
