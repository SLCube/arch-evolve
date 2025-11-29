package com.playground.payment.controller

import com.playground.payment.persistence.entity.PaymentMethodJpaEntity
import com.playground.payment.persistence.repository.PaymentMethodRepository
import com.playground.payment.presentation.request.PaymentMethodRegisterRequestDto
import com.playground.support.ApiTest
import com.playground.support.docs.performAndDocument
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@Suppress("NonAsciiCharacters")
class PaymentMethodRegisterApiTest(
    @param:Autowired private val paymentMethodRepository: PaymentMethodRepository,
) : ApiTest() {

    @Test
    fun `결제 수단 등록 - 성공, 새 카드를 등록하고 기본 카드가 전환된다`() {
        val user = createUser("userRegister", "password123", "테스트유저")
        val oldMethod = createPaymentMethod(user.id!!)

        val newRegisterRequest = PaymentMethodRegisterRequestDto(
            authKey = "SUCCESS_ISSUE_KEY",
            cardCompany = "NewCard",
            cardNumberMasked = "****-8888",
            setAsDefault = true
        )
        val jwtToken = getAccessToken(user.loginId, "password123")

        performAndDocument("결제 수단 등록 - 성공 (기본 전환)") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/payment-methods"
            requestBody = newRegisterRequest
            accessToken = jwtToken
            expectedStatus = status().isCreated
            snippets = arrayOf(
                requestFields(
                    fieldWithPath("authKey").description("PG사에서 발급받은 1회성 인증 키"),
                    fieldWithPath("cardCompany").description("카드사 이름 (ex: 신한, 국민)"),
                    fieldWithPath("cardNumberMasked").description("마스킹된 카드 번호 (표시용)"),
                    fieldWithPath("setAsDefault").description("기본 결제 수단으로 설정할지 여부 (true/false)"),
                ),
                responseFields(
                    fieldWithPath("id").description("등록된 결제 수단 ID (PaymentMethod ID)"),
                    fieldWithPath("cardCompany").description("등록된 카드사명"),
                    fieldWithPath("cardNumberMasked").description("마스킹된 카드 번호"),
                    fieldWithPath("isDefault").description("기본 결제 수단 여부"),
                    fieldWithPath("createdAt").description("등록 일시"),
                ),
            )
        }

        val newDefaultMethod = paymentMethodRepository.findDefaultByUserId(user.id!!).get()
        newDefaultMethod.cardCompany shouldBe "NewCard"
        newDefaultMethod.isDefault shouldBe true

        val oldMethodInDb = paymentMethodRepository.findById(oldMethod.id!!).get()
        oldMethodInDb.isDefault shouldBe false
    }

    fun createPaymentMethod(userId: Long): PaymentMethodJpaEntity {
        val dummyBillingKey = "bil_test_${userId}_${UUID.randomUUID().toString().substring(0, 5)}"

        val entity = PaymentMethodJpaEntity(
            userId = userId,
            billingKey = dummyBillingKey,
            cardCompany = "TestBank",
            cardNumberMasked = "****-0000",
            isDefault = true
        )
        return paymentMethodRepository.save(entity)
    }
}