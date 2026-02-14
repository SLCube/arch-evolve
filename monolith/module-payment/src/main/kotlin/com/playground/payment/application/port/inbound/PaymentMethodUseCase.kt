package com.playground.payment.application.port.inbound

import com.playground.payment.application.port.inbound.command.PaymentMethodDeleteCommand
import com.playground.payment.application.port.inbound.command.PaymentMethodRegisterCommand
import com.playground.payment.domain.model.PaymentMethod

interface PaymentMethodUseCase {
    fun registerPaymentMethod(command: PaymentMethodRegisterCommand) : PaymentMethod
    fun getPaymentMethodList(userId: Long) : List<PaymentMethod>
    fun deletePaymentMethod(command: PaymentMethodDeleteCommand)
}