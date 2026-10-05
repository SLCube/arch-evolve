package com.playground.product.application.port.outbound

data class StockReservationSnapshot(val orderId: Long, val quantities: Map<Long, Int>)
