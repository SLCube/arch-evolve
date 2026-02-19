package com.playground.payment.presentation.web.controller

import com.playground.payment.presentation.web.annotation.PaymentMethodControllerSliceTest
import com.playground.support.RestDocsTest
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@PaymentMethodControllerSliceTest
class PaymentMethodDeleteApiTest : RestDocsTest() {
    @Test
    fun `결제 수단 삭제 - 성공`() {
        val paymentMethodId = 20L

        performAndDocument("결제 수단 삭제 - 성공") {
            tag = "결제 수단 관리"
            summary = "결제 수단 삭제"
            description = "등록된 결제 수단을 삭제합니다. 삭제 성공 시 204 No Content를 반환합니다."
            httpMethod = HttpMethod.DELETE
            urlTemplate = "/payment-methods/{paymentMethodId}"
            urlVars = arrayOf(paymentMethodId)
            userId = 7L
            expectedStatus = status().isNoContent
        }
    }
}
