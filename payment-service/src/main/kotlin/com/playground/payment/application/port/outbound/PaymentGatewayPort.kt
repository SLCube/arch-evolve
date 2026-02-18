package com.playground.payment.application.port.outbound

import com.playground.payment.domain.vo.PgAuthorizationResult
import java.math.BigDecimal

interface PaymentGatewayPort {
    fun requestAuthorization(
        paymentKey: String,
        amount: BigDecimal,
    ): PgAuthorizationResult

    fun issueBillingKey(
        authKey: String,
        userId: Long,
    ): String
}
