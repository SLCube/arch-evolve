package com.playground.payment.application.port.inbound

import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import com.playground.payment.domain.model.Payment

fun interface PaymentUseCase {
    fun authorizePayment(command: PaymentAuthorizeCommand) : Payment
}