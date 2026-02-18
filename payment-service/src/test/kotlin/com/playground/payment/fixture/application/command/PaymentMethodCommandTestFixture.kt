package com.playground.payment.fixture.application.command

import com.playground.payment.application.port.inbound.command.PaymentMethodDeleteCommand
import com.playground.payment.application.port.inbound.command.PaymentMethodRegisterCommand

object PaymentMethodCommandTestFixture {
    fun registerCommand(
        userId: Long = 2L,
        authKey: String = "auth-key",
        cardCompany: String = "테스트카드",
        cardNumberMasked: String = "****-1111",
        setAsDefault: Boolean = false,
    ) = PaymentMethodRegisterCommand(
        userId = userId,
        authKey = authKey,
        cardCompany = cardCompany,
        cardNumberMasked = cardNumberMasked,
        setAsDefault = setAsDefault,
    )

    fun deleteCommand(
        userId: Long = 2L,
        paymentMethodId: Long = 1L,
    ) = PaymentMethodDeleteCommand(
        userId = userId,
        paymentMethodId = paymentMethodId,
    )
}
