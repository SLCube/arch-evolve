package com.playground.user.presentation.request

import com.playground.support.validate.ValidateTest
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import jakarta.validation.constraints.NotBlank
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class UserSignUpRequestTest: ValidateTest() {

    @Test
    fun `loginId없이 회원가입을 할 수 없다`() {
        val request = UserSignUpRequestDto(
            loginId = "",
            password = "password",
            nickname = "nickname",
        )

        val validations = validator.validate(request)

        validations shouldHaveSize 2

        val notBlankViolation = validations.first { it.constraintDescriptor.annotation.annotationClass.java == NotBlank::class.java }
        notBlankViolation.propertyPath.toString() shouldBe "loginId"
    }

    @Test
    fun `password없이 회원가입을 할 수 없다`() {
        val request = UserSignUpRequestDto(
            loginId = "validId",
            password = "",
            nickname = "nickname",
        )

        val validations = validator.validate(request)

        validations shouldHaveSize 2
        val notBlankViolation = validations.first { it.constraintDescriptor.annotation.annotationClass.java == NotBlank::class.java }
        notBlankViolation.propertyPath.toString() shouldBe "password"
    }

    @Test
    fun `nickname없이 회원가입을 할 수 없다`() {
        val request = UserSignUpRequestDto(
            loginId = "validId",
            password = "password",
            nickname = "",
        )

        val validations = validator.validate(request)

        validations shouldHaveSize 2
        val notBlankViolation = validations.first { it.constraintDescriptor.annotation.annotationClass.java == NotBlank::class.java }
        notBlankViolation.propertyPath.toString() shouldBe "nickname"
    }
}