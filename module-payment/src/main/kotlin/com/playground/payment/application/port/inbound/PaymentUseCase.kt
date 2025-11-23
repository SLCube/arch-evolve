package com.playground.payment.application.port.inbound

import com.playground.payment.application.port.inbound.command.AuthorizePaymentCommand
import com.playground.payment.domain.model.Payment

interface PaymentUseCase {
    fun authorizePayment(command: AuthorizePaymentCommand) : Payment
}