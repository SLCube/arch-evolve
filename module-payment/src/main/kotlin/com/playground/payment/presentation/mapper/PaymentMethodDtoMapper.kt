package com.playground.payment.presentation.mapper

import com.playground.payment.application.port.inbound.command.PaymentMethodRegisterCommand
import com.playground.payment.presentation.request.PaymentMethodRegisterRequestDto

fun PaymentMethodRegisterRequestDto.toCommand(userId: Long) : PaymentMethodRegisterCommand {
    return PaymentMethodRegisterCommand(
        userId = userId,
        authKey = authKey,
        cardCompany = cardCompany,
        cardNumberMasked = cardNumberMasked,
        setAsDefault = setAsDefault,
    )
}