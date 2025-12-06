package com.playground.order.presentation.controller

import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.OrderQueryUseCase
import com.playground.order.fixture.OrderTestFixture
import com.playground.order.presentation.config.OrderControllerTestConfig
import com.playground.order.presentation.web.OrderController
import com.playground.support.RestDocsTest
import com.playground.support.docs.performAndDocument
import com.playground.support.security.annotation.WithMockAuthUser
import com.playground.support.security.config.TestSecurityConfig
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@WebMvcTest(OrderController::class)
@Import(OrderControllerTestConfig::class, TestSecurityConfig::class)
class OrderCreateApiTest: RestDocsTest() {

    @Autowired
    private lateinit var orderCommandUseCase: OrderCommandUseCase

    @Autowired
    private lateinit var orderQueryUseCase: OrderQueryUseCase

    @Test
    @WithMockAuthUser(userId = 2L)
    fun `주문 생성 - 성공`() {
        val orderCreateRequestDto = OrderTestFixture.createOrderRequest()

        val mockOrder = OrderTestFixture.mockOrder()

        given(orderCommandUseCase.createOrder(any()))
            .willReturn(mockOrder)

        performAndDocument("주문 생성 - 성공") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/orders"
            requestBody = orderCreateRequestDto
            expectedStatus = status().isCreated
            additionalMatchers =
                arrayOf(
                    jsonPath("$.id").value(mockOrder.id),
                    jsonPath("$.userId").value(mockOrder.userId),
                    jsonPath("$.totalPrice").value(mockOrder.totalPrice),
                    jsonPath("$.status").value(mockOrder.status.name),
                    jsonPath("$.orderProducts.length()").value(mockOrder.orderProducts.size),
                    jsonPath("$.orderProducts[0].productId").value(mockOrder.orderProducts[0].productId),
                    jsonPath("$.orderProducts[0].quantity").value(mockOrder.orderProducts[0].quantity),
                    jsonPath("$.orderProducts[0].price").value(mockOrder.orderProducts[0].price),
                    jsonPath("$.orderProducts[1].productId").value(mockOrder.orderProducts[1].productId),
                    jsonPath("$.orderProducts[1].quantity").value(mockOrder.orderProducts[1].quantity),
                    jsonPath("$.orderProducts[1].price").value(mockOrder.orderProducts[1].price),
                )
        }
    }
}