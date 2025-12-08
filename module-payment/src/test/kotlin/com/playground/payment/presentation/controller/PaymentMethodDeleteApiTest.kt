package com.playground.payment.presentation.controller

import com.playground.payment.presentation.annotation.PaymentControllerSliceTest
import com.playground.support.RestDocsTest
import com.playground.support.docs.performAndDocument
import com.playground.support.security.annotation.WithMockAuthUser
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@PaymentControllerSliceTest
class PaymentMethodDeleteApiTest : RestDocsTest() {

    @Test
    @WithMockAuthUser(userId = 7L)
    fun `결제 수단 삭제 - 성공`() {
        val paymentMethodId = 20L

        performAndDocument("결제 수단 삭제 - 성공") {
            httpMethod = HttpMethod.DELETE
            urlTemplate = "/payment-methods/{paymentMethodId}"
            urlVars = arrayOf(paymentMethodId)
            expectedStatus = status().isNoContent
        }
    }
}
