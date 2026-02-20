package com.playground.payment.presentation.internal.controller

import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.domain.enum.PaymentStatus
import com.playground.payment.fixture.application.domain.PaymentDomainTestFixture
import com.playground.payment.fixture.internal.request.PaymentAuthorizeRequestTestFixture
import com.playground.payment.presentation.internal.annotation.PaymentInternalControllerSliceTest
import com.playground.support.RestDocsTest
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@PaymentInternalControllerSliceTest
class PaymentAuthorizeApiTest(
    @param:Autowired private val paymentUseCase: PaymentUseCase,
) : RestDocsTest() {
    @Test
    fun `결제 승인 - 성공`() {
        val request = PaymentAuthorizeRequestTestFixture.authorizeRequest()
        val mockPayment =
            PaymentDomainTestFixture.mockPayment(
                id = 1L,
                userId = request.userId,
                orderId = request.orderId,
                amount = request.amount,
                status = PaymentStatus.COMPLETED,
                pgTransactionId = "PG_TX_123456",
                approvalNumber = "APPROVAL_789",
            )

        given(paymentUseCase.authorizePayment(any())).willReturn(mockPayment)

        performAndDocument("결제 승인 - 성공") {
            tag = "결제 (Internal)"
            summary = "결제 승인"
            description = "주문 서비스에서 결제 승인을 요청합니다. Gateway를 거치지 않는 내부 API입니다."
            httpMethod = HttpMethod.POST
            urlTemplate = "/internal/payments/authorize"
            requestBody = request
            expectedStatus = status().isCreated
            additionalMatchers =
                arrayOf(
                    jsonPath("$.paymentId").value(mockPayment.id),
                    jsonPath("$.orderId").value(mockPayment.orderId),
                    jsonPath("$.pgTransactionId").value(mockPayment.pgTransactionId),
                    jsonPath("$.approvalNumber").value(mockPayment.approvalNumber),
                    jsonPath("$.status").value(PaymentStatus.COMPLETED.name),
                )
            snippets =
                arrayOf(
                    requestFields(
                        fieldWithPath("userId").description("사용자 ID"),
                        fieldWithPath("orderId").description("주문 ID"),
                        fieldWithPath("amount").description("결제 금액"),
                    ),
                    responseFields(
                        fieldWithPath("paymentId").description("결제 ID"),
                        fieldWithPath("orderId").description("주문 ID"),
                        fieldWithPath("pgTransactionId").description("PG사 거래 ID"),
                        fieldWithPath("approvalNumber").description("승인 번호"),
                        fieldWithPath("status").description("결제 상태"),
                    ),
                )
        }
    }
}
