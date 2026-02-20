package com.playground.payment.fixture.internal.request

import com.playground.payment.presentation.internal.request.PaymentAuthorizeRequestDto
import java.math.BigDecimal

object PaymentAuthorizeRequestTestFixture {
    fun authorizeRequest(
        userId: Long = 2L,
        orderId: Long = 100L,
        amount: BigDecimal = BigDecimal("10000.00"),
    ) = PaymentAuthorizeRequestDto(
        userId = userId,
        orderId = orderId,
        amount = amount,
    )
}
