package com.playground.payment.presentation.web.controller

import com.playground.payment.application.port.inbound.PaymentMethodUseCase
import com.playground.payment.fixture.domain.PaymentMethodDomainTestFixture
import com.playground.payment.presentation.web.annotation.PaymentMethodControllerSliceTest
import com.playground.support.RestDocsTest
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@PaymentMethodControllerSliceTest
class PaymentMethodLookupApiTest(
    @param:Autowired private val paymentMethodUseCase: PaymentMethodUseCase,
) : RestDocsTest() {
    @Test
    fun `결제 수단 목록 조회 - 성공`() {
        val paymentMethods =
            listOf(
                PaymentMethodDomainTestFixture.mockPaymentMethod(id = 1L, userId = 3L, cardCompany = "카드A", isDefault = true),
                PaymentMethodDomainTestFixture.mockPaymentMethod(id = 2L, userId = 3L, cardCompany = "카드B", isDefault = false),
            )

        given(paymentMethodUseCase.getPaymentMethodList(3L))
            .willReturn(paymentMethods)

        performAndDocument("결제 수단 목록 조회 - 성공") {
            tag = "결제 수단 관리"
            summary = "결제 수단 목록 조회"
            description = "인증된 사용자의 등록된 결제 수단 목록을 조회합니다."
            httpMethod = HttpMethod.GET
            urlTemplate = "/payment-methods"
            userId = 3L
            expectedStatus = status().isOk
            additionalMatchers =
                arrayOf(
                    jsonPath("$.length()").value(paymentMethods.size),
                    jsonPath("$[0].id").value(paymentMethods[0].id),
                    jsonPath("$[0].cardCompany").value("카드A"),
                    jsonPath("$[0].isDefault").value(true),
                    jsonPath("$[1].id").value(paymentMethods[1].id),
                    jsonPath("$[1].cardCompany").value("카드B"),
                    jsonPath("$[1].isDefault").value(false),
                )
            snippets =
                arrayOf(
                    responseFields(
                        fieldWithPath("[].id").description("결제 수단 ID"),
                        fieldWithPath("[].cardCompany").description("카드사 이름"),
                        fieldWithPath("[].cardNumberMasked").description("마스킹된 카드번호"),
                        fieldWithPath("[].isDefault").description("기본 결제 수단 여부"),
                        fieldWithPath("[].createdAt").description("등록 일시"),
                    ),
                )
        }
    }
}
