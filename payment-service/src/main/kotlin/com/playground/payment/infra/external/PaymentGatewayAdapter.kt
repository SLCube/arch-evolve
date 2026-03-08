package com.playground.payment.infra.external

import com.playground.payment.application.port.outbound.PaymentGatewayPort
import com.playground.payment.common.log.utils.logger
import com.playground.payment.domain.exception.BusinessException
import com.playground.payment.domain.exception.PaymentGatewayTimeoutException
import com.playground.payment.domain.exception.PaymentLimitExceededException
import com.playground.payment.domain.vo.PgAuthorizationResult
import com.playground.payment.infra.external.dto.BillingKeyResponse
import com.playground.payment.infra.external.dto.PgAuthorizeResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.math.BigDecimal

@Component
class PaymentGatewayAdapter(
    private val pgRestClient: RestClient,
) : PaymentGatewayPort {
    private val log = logger()

    override fun issueBillingKey(
        authKey: String,
        userId: Long,
    ): String {
        if (authKey.startsWith("FAIL_ISSUE_TIMEOUT")) {
            throw PaymentGatewayTimeoutException()
        }

        val response =
            pgRestClient
                .post()
                .uri("/pg/billing-key")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("authKey" to authKey, "userId" to userId))
                .retrieve()
                .body(BillingKeyResponse::class.java)
                ?: throw PaymentGatewayTimeoutException()

        return response.billingKey
    }

    override fun requestAuthorization(
        paymentKey: String,
        amount: BigDecimal,
    ): PgAuthorizationResult =
        try {
            if (paymentKey.startsWith("FAIL_")) {
                throw PaymentGatewayTimeoutException()
            }
            if (amount >= BigDecimal("10000000")) {
                throw PaymentLimitExceededException()
            }

            val response =
                pgRestClient
                    .post()
                    .uri("/pg/authorize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(mapOf("paymentKey" to paymentKey, "amount" to amount))
                    .retrieve()
                    .body(PgAuthorizeResponse::class.java)
                    ?: throw PaymentGatewayTimeoutException()

            PgAuthorizationResult(
                isSuccess = true,
                pgTransactionId = response.pgTransactionId,
                approvalNumber = response.approvalNumber,
                failReason = null,
            )
        } catch (e: Exception) {
            val errorMessage =
                when (e) {
                    is BusinessException -> e.message
                    else -> "PG 연동 오류 ${e.message}"
                }
            log.warn("PG 결제 승인 실패 [reason={}]", errorMessage, e)
            PgAuthorizationResult(
                isSuccess = false,
                pgTransactionId = null,
                approvalNumber = null,
                failReason = errorMessage,
            )
        }
}
