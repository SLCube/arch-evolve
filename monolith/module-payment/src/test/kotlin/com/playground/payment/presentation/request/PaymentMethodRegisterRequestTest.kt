package com.playground.payment.presentation.request

import com.playground.support.validate.ValidateTest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import jakarta.validation.constraints.NotBlank
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class PaymentMethodRegisterRequestTest : ValidateTest() {

    @Test
    fun `필수값이 비어있으면 결제수단 등록 요청이 유효하지 않다`() {
        val request =
            PaymentMethodRegisterRequestDto(
                authKey = "",
                cardCompany = "",
                cardNumberMasked = "",
                setAsDefault = true,
            )

        val validations = validator.validate(request)

        validations shouldHaveSize 3
        validations.map { it.propertyPath.toString() }.toSet() shouldBe setOf("authKey", "cardCompany", "cardNumberMasked")
        validations.forEach { it.constraintDescriptor.annotation.annotationClass.java shouldBe NotBlank::class.java }
    }
}
