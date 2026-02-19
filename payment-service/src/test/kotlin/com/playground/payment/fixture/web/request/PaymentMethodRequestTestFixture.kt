package com.playground.payment.fixture.web.request

import com.playground.payment.presentation.web.request.PaymentMethodRegisterRequestDto

object PaymentMethodRequestTestFixture {
    fun registerRequest(
        authKey: String = "SUCCESS_AUTH_KEY",
        cardCompany: String = "테스트카드",
        cardNumberMasked: String = "****-1234",
        setAsDefault: Boolean = true,
    ) = PaymentMethodRegisterRequestDto(
        authKey = authKey,
        cardCompany = cardCompany,
        cardNumberMasked = cardNumberMasked,
        setAsDefault = setAsDefault,
    )
}
