package com.playground.order.presentation.request

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import jakarta.validation.Validation
import jakarta.validation.Validator
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Size
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class OrderCreateRequestTest {

    private lateinit var validator: Validator

    @BeforeEach
    fun setUp() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @Test
    fun `상품없이 주문을 생성할 수 없다`() {
        val request = OrderCreateRequestDto(
            addressId = 1L,
            orderProducts = mutableListOf()
        )

        val validations = validator.validate(request)

        validations shouldHaveSize 1

        val validation = validations.first()
        validation.propertyPath.toString() shouldBe "orderProducts"
        validation.constraintDescriptor.annotation.annotationClass.java shouldBe Size::class.java
    }

    @Test
    fun `주문 생성시 상품갯수 0개로 주문할 수 없다`() {
        val request = OrderCreateRequestDto(
            addressId = 1L,
            orderProducts = listOf(
                OrderProductRequestDto(
                    productId = 1L,
                    quantity = 0
                )
            )
        )

        val validations = validator.validate(request)
        validations shouldHaveSize 1

        val validation = validations.first()
        validation.propertyPath.toString() shouldBe "orderProducts[0].quantity"
        validation.constraintDescriptor.annotation.annotationClass.java shouldBe Min::class.java
    }
}