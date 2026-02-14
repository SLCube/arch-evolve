package com.playground.payment.fixture.application.command

import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import java.math.BigDecimal

object PaymentCommandTestFixture {
    fun authorizeCommand(
        userId: Long = 2L,
        orderId: Long = 100L,
        amount: BigDecimal = BigDecimal("10000.00"),
    ) = PaymentAuthorizeCommand(
        userId = userId,
        orderId = orderId,
        amount = amount,
    )
}
