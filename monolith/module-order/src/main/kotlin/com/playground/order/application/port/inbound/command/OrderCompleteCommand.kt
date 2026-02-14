package com.playground.order.application.port.inbound.command

import java.math.BigDecimal

data class OrderCompleteCommand(
    val orderId: Long,
    val pgTransactionId: String,
    val paidAmount: BigDecimal
)
