package com.playground.payment.presentation.web.controller

import com.playground.payment.application.port.inbound.PaymentMethodUseCase
import com.playground.payment.fixture.domain.PaymentMethodDomainTestFixture
import com.playground.payment.fixture.web.request.PaymentMethodRequestTestFixture
import com.playground.payment.presentation.web.annotation.PaymentMethodControllerSliceTest
import com.playground.support.RestDocsTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
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
import java.time.format.DateTimeFormatter

@Suppress("NonAsciiCharacters")
@PaymentMethodControllerSliceTest
class PaymentMethodRegisterApiTest(
    @param:Autowired private val paymentMethodUseCase: PaymentMethodUseCase,
) : RestDocsTest() {
    @Test
    fun `결제 수단 등록 - 성공`() {
        val request = PaymentMethodRequestTestFixture.registerRequest()
        val mockPaymentMethod =
            PaymentMethodDomainTestFixture.mockPaymentMethod(
                id = 11L,
                userId = 5L,
                cardCompany = request.cardCompany,
                cardNumberMasked = request.cardNumberMasked,
                isDefault = true,
            )
        val expectedCreatedAt = mockPaymentMethod.createdAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        given(paymentMethodUseCase.registerPaymentMethod(any()))
            .willReturn(mockPaymentMethod)

        performAndDocument("결제 수단 등록 - 성공") {
            tag = "결제 수단 관리"
            summary = "결제 수단 등록"
            description = "PG사 인증을 통해 발급받은 인증 키와 카드 정보를 이용하여 새로운 결제 수단을 등록합니다."
            httpMethod = HttpMethod.POST
            urlTemplate = "/payment-methods"
            userId = 5L
            requestBody = request
            expectedStatus = status().isCreated
            additionalMatchers =
                arrayOf(
                    jsonPath("$.id").value(mockPaymentMethod.id),
                    jsonPath("$.cardCompany").value(mockPaymentMethod.cardCompany),
                    jsonPath("$.cardNumberMasked").value(mockPaymentMethod.cardNumberMasked),
                    jsonPath("$.isDefault").value(true),
                    jsonPath("$.createdAt").value(expectedCreatedAt),
                )
            snippets =
                arrayOf(
                    requestFields(
                        fieldWithPath("authKey").description("PG사 인증을 통해 발급받은 인증 키"),
                        fieldWithPath("cardCompany").description("카드사 이름"),
                        fieldWithPath("cardNumberMasked").description("마스킹된 카드번호"),
                        fieldWithPath("setAsDefault").description("기본 결제 수단 지정 여부"),
                    ),
                    responseFields(
                        fieldWithPath("id").description("결제 수단 ID"),
                        fieldWithPath("cardCompany").description("카드사 이름"),
                        fieldWithPath("cardNumberMasked").description("마스킹된 카드번호"),
                        fieldWithPath("isDefault").description("기본 결제 수단 여부"),
                        fieldWithPath("createdAt").description("등록 일시"),
                    ),
                )
        }
    }

    @Test
    fun `결제 수단 등록 - 필수 항목이 비어있음`() {
        val invalidRequest =
            PaymentMethodRequestTestFixture.registerRequest(
                authKey = "",
                cardCompany = "",
                cardNumberMasked = "",
            )

        performAndDocument("결제 수단 등록 - 필수 항목이 비어있음") {
            tag = "결제 수단 관리"
            summary = "결제 수단 등록"
            description = "PG사 인증을 통해 발급받은 인증 키와 카드 정보를 이용하여 새로운 결제 수단을 등록합니다."
            httpMethod = HttpMethod.POST
            urlTemplate = "/payment-methods"
            userId = 5L
            requestBody = invalidRequest
            expectedStatus = status().isBadRequest
            additionalMatchers =
                arrayOf(
                    jsonPath("$.message").value("입력값이 유효하지 않습니다."),
                    jsonPath("$.errors.authKey").isNotEmpty,
                    jsonPath("$.errors.cardCompany").isNotEmpty,
                    jsonPath("$.errors.cardNumberMasked").isNotEmpty,
                )
            snippets =
                arrayOf(
                    responseFields(
                        commonErrorResponseSnippet() +
                            fieldWithPath("errors.cardCompany").description("카드사 필드 에러 메시지") +
                            fieldWithPath("errors.cardNumberMasked").description("카드번호 필드 에러 메시지") +
                            fieldWithPath("errors.authKey").description("인증키 필드 에러 메시지"),
                    ),
                )
        }
    }
}
