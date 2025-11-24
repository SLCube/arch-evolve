package com.playground.payment.infra.external

import com.playground.payment.application.port.outbound.PaymentGatewayPort
import com.playground.payment.domain.vo.PgAuthorizationResult
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PaymentGatewayAdapter(

) : PaymentGatewayPort {
    override fun requestAuthorization(
        paymentKey: String,
        amount: BigDecimal
    ): PgAuthorizationResult {
        TODO("Not yet implemented")
    }
}