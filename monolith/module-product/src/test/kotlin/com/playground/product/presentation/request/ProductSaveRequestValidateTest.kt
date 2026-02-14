package com.playground.product.presentation.request

import com.playground.support.validate.ValidateTest
import io.kotest.matchers.shouldBe
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class ProductSaveRequestValidateTest: ValidateTest() {

    @Test
    fun `삼품 등록 시 이름이 비어있을 수 없다`() {
        val request = ProductSaveRequestDto(
            name = "",
            stock = 10,
            price = 10000.toBigDecimal(),
        )

        val validate = validator.validate(request)

        validate.size shouldBe 1
        val validation = validate.first()
        validation.propertyPath.toString() shouldBe "name"
        validation.constraintDescriptor.annotation.annotationClass.java shouldBe NotBlank::class.java
    }

    @Test
    fun `상품 등록 시 재고가 0보다 작을 수 없다`() {
        val request = ProductSaveRequestDto(
            name = "상품1",
            stock = -1,
            price = 10000.toBigDecimal()
        )

        val validate = validator.validate(request)

        validate.size shouldBe 1
        val validation = validate.first()
        validation.propertyPath.toString() shouldBe "stock"
        validation.constraintDescriptor.annotation.annotationClass.java shouldBe PositiveOrZero::class.java
    }

    @Test
    fun `상품 등록 시 가격이 0보다 작을 수 없다`() {
        val request = ProductSaveRequestDto(
            name = "상품1",
            stock = 10,
            price = (-1).toBigDecimal()
        )
        val validate = validator.validate(request)

        validate.size shouldBe 1
        val validation = validate.first()
        validation.propertyPath.toString() shouldBe "price"
        validation.constraintDescriptor.annotation.annotationClass.java shouldBe PositiveOrZero::class.java
    }
}