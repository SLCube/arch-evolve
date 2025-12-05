package com.playground.order.presentation.request

import io.kotest.matchers.collections.shouldHaveSize
import jakarta.validation.Validation
import jakarta.validation.Validator
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

        val result = validator.validate(request)

        result shouldHaveSize 1
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

        val result = validator.validate(request)
        result shouldHaveSize 1
    }
}