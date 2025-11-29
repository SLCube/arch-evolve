package com.playground.payment.controller

import com.playground.common.error.ErrorCode
import com.playground.payment.persistence.entity.PaymentMethodJpaEntity
import com.playground.payment.persistence.repository.PaymentMethodRepository
import com.playground.support.ApiTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.*

@Suppress("NonAsciiCharacters")
class PaymentMethodDeleteApiTest(
    @param:Autowired private val paymentMethodRepository: PaymentMethodRepository,
) : ApiTest() {

    @Test
    fun `결제 수단 삭제 - 성공`() {
        val user = createUser("user", "password123", "유저")
        val jwtToken = getAccessToken(user.loginId, "password123")
        val userMethod = createPaymentMethod(user.id!!)
        val methodId = userMethod.id!!

        performAndDocument("결제 수단 삭제 - 성공") {
            httpMethod = HttpMethod.DELETE
            urlTemplate = "/payment-methods/{paymentMethodId}"
            urlVars = arrayOf(methodId)
            accessToken = jwtToken

            expectedStatus = status().isNoContent

            snippets = arrayOf(
                pathParameters(
                    parameterWithName("paymentMethodId").description("삭제할 결제 수단 ID"),
                ),
            )
        }

        val deletedEntity = paymentMethodRepository.findById(methodId)
        deletedEntity.isPresent shouldBe false
    }

    @Test
    fun `결제 수단 삭제 - 실패, 다른 사용자의 리소스 접근 시도 (IDOR)`() {
        val ownerUser = createUser("owner", "password", "소유자")
        val otherUser = createUser("attacker", "password", "공격자")

        val ownerMethod = createPaymentMethod(ownerUser.id!!)
        val ownerMethodId = ownerMethod.id!!

        // Given: 공격자 토큰 발급
        val attackerJwtToken = getAccessToken(otherUser.loginId, "password")

        performAndDocument("결제 수단 삭제 - 실패 (IDOR)") {
            httpMethod = HttpMethod.DELETE
            urlTemplate = "/payment-methods/{paymentMethodId}"
            urlVars = arrayOf(ownerMethodId)
            accessToken = attackerJwtToken
            expectedStatus = status().isForbidden
            additionalMatchers =
                arrayOf(
                    jsonPath("$.code").value(ErrorCode.PAYMENT_ACCESS_DENIED.code),
                )
            snippets = arrayOf(
                pathParameters(
                    parameterWithName("paymentMethodId").description("다른 사용자의 결제수단 ID"),
                ),
                responseFields(commonErrorResponseSnippet()),
            )
        }
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