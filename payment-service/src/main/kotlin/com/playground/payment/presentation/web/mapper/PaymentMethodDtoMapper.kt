package com.playground.payment.presentation.web.mapper

import com.playground.payment.application.port.inbound.command.PaymentMethodRegisterCommand
import com.playground.payment.presentation.web.request.PaymentMethodRegisterRequestDto

fun PaymentMethodRegisterRequestDto.toCommand(userId: Long): PaymentMethodRegisterCommand =
    PaymentMethodRegisterCommand(
        userId = userId,
        authKey = authKey,
        cardCompany = cardCompany,
        cardNumberMasked = cardNumberMasked,
        setAsDefault = setAsDefault,
    )
