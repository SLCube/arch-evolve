package com.playground.payment.application.port.inbound.command

import java.math.BigDecimal

data class AuthorizePaymentCommand(
    val userId: Long,
    val orderId: Long,
    val amount: BigDecimal,
)
