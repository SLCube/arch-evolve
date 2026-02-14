package com.playground.auth.presentation.request

import com.playground.support.validate.ValidateTest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import jakarta.validation.constraints.NotBlank
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class AuthLoginRequestValidateTest : ValidateTest() {

    @Test
    fun `로그인 시 loginId 는 비어있을 수 없다`() {
        val request = AuthLoginRequestDto(loginId = "", password = "password123")

        val validations = validator.validate(request)

        validations shouldHaveSize 1
        val validation = validations.first()
        validation.propertyPath.toString() shouldBe "loginId"
        validation.constraintDescriptor.annotation.annotationClass.java shouldBe NotBlank::class.java
    }

    @Test
    fun `로그인 시 password 는 비어있을 수 없다`() {
        val request = AuthLoginRequestDto(loginId = "tester", password = "")

        val validations = validator.validate(request)

        validations shouldHaveSize 1
        val validation = validations.first()
        validation.propertyPath.toString() shouldBe "password"
        validation.constraintDescriptor.annotation.annotationClass.java shouldBe NotBlank::class.java
    }
}
