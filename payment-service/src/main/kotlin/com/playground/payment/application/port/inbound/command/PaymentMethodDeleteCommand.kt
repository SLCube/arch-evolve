package com.playground.payment.application.port.inbound.command

data class PaymentMethodDeleteCommand(
    val userId: Long,
    val paymentMethodId: Long,
)
