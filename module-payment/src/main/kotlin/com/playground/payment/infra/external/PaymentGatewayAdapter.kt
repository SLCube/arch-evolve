package com.playground.payment.infra.external

import com.playground.common.error.BusinessException
import com.playground.payment.application.port.outbound.PaymentGatewayPort
import com.playground.payment.domain.exception.PaymentGatewayTimeoutException
import com.playground.payment.domain.exception.PaymentLimitExceededException
import com.playground.payment.domain.vo.PgAuthorizationResult
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID
import kotlin.random.Random

@Component
class PaymentGatewayAdapter : PaymentGatewayPort {
    override fun requestAuthorization(
        paymentKey: String,
        amount: BigDecimal
    ): PgAuthorizationResult {
        return try {
            callExternalPgApi(paymentKey, amount)

            PgAuthorizationResult(
                isSuccess = true,
                pgTransactionId = "TID_" + UUID.randomUUID().toString().substring(0, 8),
                approvalNumber = Random.nextInt(10000000, 99999999).toString(),
                failReason = null
            )
        } catch (e: Exception) {
            val errorMessage = when (e) {
                is BusinessException -> e.message
                else -> "PG 연동 오류 ${e.message}"
            }
            PgAuthorizationResult(
                isSuccess = false,
                pgTransactionId = null,
                approvalNumber = null,
                failReason = errorMessage
            )
        }
    }

    private fun callExternalPgApi(paymentKey: String, amount: BigDecimal) {
        val latency = Random.nextLong(300, 500)
        Thread.sleep(latency)

        if (paymentKey.startsWith("FAIL_")) {
            throw PaymentGatewayTimeoutException()
        }

        if (amount >= BigDecimal("10000000")) {
            throw PaymentLimitExceededException()
        }
    }
}