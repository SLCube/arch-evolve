package com.playground.payment.presentation.internal.request

import com.playground.payment.application.port.inbound.command.PaymentAuthorizeCommand
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

data class PaymentAuthorizeRequestDto(
    val userId: Long,
    val orderId: Long,
    @field:Positive val amount: BigDecimal,
) {
    fun toCommand(): PaymentAuthorizeCommand =
        PaymentAuthorizeCommand(
            userId = userId,
            orderId = orderId,
            amount = amount,
        )
}
