package com.playground.payment.application.port.inbound.command

data class PaymentMethodRegisterCommand(
    val userId: Long,
    val authKey: String,
    val cardCompany: String,
    val cardNumberMasked: String,
    val setAsDefault: Boolean = false,
)
