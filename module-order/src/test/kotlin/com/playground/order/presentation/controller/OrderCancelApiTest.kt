package com.playground.order.presentation.controller

import com.playground.common.error.ErrorCode
import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.command.OrderCancelCommand
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.domain.exception.OrderAccessDeniedException
import com.playground.order.domain.exception.OrderNotFoundException
import com.playground.order.domain.exception.OrderStatusInvalidException
import com.playground.order.fixture.application.domain.OrderDomainTestFixture
import com.playground.order.presentation.annotation.OrderControllerSliceTest
import com.playground.support.RestDocsTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import com.playground.support.security.annotation.WithMockAuthUser
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@OrderControllerSliceTest
class OrderCancelApiTest(
    @param:Autowired private val orderCommandUseCase: OrderCommandUseCase,
) : RestDocsTest() {

    @Test
    @WithMockAuthUser(userId = 2L)
    fun `주문 취소 - 성공`() {
        val mockOrder = OrderDomainTestFixture.mockOrder()
        mockOrder.cancelOrder()

        val authenticatedUserId = 2L
        val command = OrderCancelCommand(
            userId = authenticatedUserId,
            orderId = mockOrder.id!!,
        )

        given(orderCommandUseCase.cancelOrder(eq(command)))
            .willReturn(mockOrder)

        performAndDocument("주문 취소 - 성공") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/orders/{orderId}/cancel"
            urlVars = arrayOf(mockOrder.id)
            expectedStatus = status().isOk
            additionalMatchers =
                arrayOf(
                    jsonPath("$.id").value(mockOrder.id),
                    jsonPath("$.status").value(OrderStatus.CANCELLED.name),
                )
            snippets = arrayOf(
                pathParameters(
                    parameterWithName("orderId").description("취소할 주문 ID"),
                ),
                responseFields(
                    fieldWithPath("id").description("주문 ID"),
                    fieldWithPath("userId").description("주문 사용자 ID"),
                    fieldWithPath("totalPrice").description("총 주문 금액"),
                    fieldWithPath("status").description("주문 상태"),
                    fieldWithPath("orderProducts[].id").description("주문 상품 ID"),
                    fieldWithPath("orderProducts[].productId").description("상품 ID"),
                    fieldWithPath("orderProducts[].quantity").description("주문 수량"),
                    fieldWithPath("orderProducts[].price").description("상품 가격"),
                )
            )
        }
    }

    @Test
    @WithMockAuthUser(userId = 2L)
    fun `주문 취소 - 실패 (존재하지 않는 주문)`() {
        val authenticatedUserId = 2L
        val nonExistingOrderId = 999L

        val command = OrderCancelCommand(
            userId = authenticatedUserId,
            orderId = nonExistingOrderId,
        )

        given(orderCommandUseCase.cancelOrder(eq(command)))
            .willThrow(OrderNotFoundException(nonExistingOrderId))

        performAndDocument("주문 취소 - 실패 (존재하지 않는 주문)") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/orders/{orderId}/cancel"
            urlVars = arrayOf(nonExistingOrderId)
            expectedStatus = status().isNotFound
            additionalMatchers =
                arrayOf(
                    jsonPath("$.code").value(ErrorCode.ORDER_NOT_FOUND.code),
                    jsonPath("$.message").value(ErrorCode.ORDER_NOT_FOUND.message(nonExistingOrderId)),
                )
            snippets = arrayOf(
                pathParameters(
                    parameterWithName("orderId").description("존재하지 않는 주문 ID"),
                ),
                responseFields(commonErrorResponseSnippet())
            )
        }
    }

    @Test
    @WithMockAuthUser(userId = 2L)
    fun `주문 취소 - 실패 (이미 취소된 주문)`() {
        val authenticatedUserId = 2L
        val alreadyCancelOrderId = 10L

        val command = OrderCancelCommand(
            userId = authenticatedUserId,
            orderId = alreadyCancelOrderId,
        )

        given(orderCommandUseCase.cancelOrder(eq(command)))
            .willThrow(OrderStatusInvalidException(OrderStatus.CANCELLED))

        performAndDocument("주문 취소 - 실패 (이미 취소된 주문)") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/orders/{orderId}/cancel"
            urlVars = arrayOf(alreadyCancelOrderId)
            expectedStatus = status().isBadRequest
            additionalMatchers =
                arrayOf(
                    jsonPath("$.code").value(ErrorCode.ORDER_STATUS_INVALID_FOR_CANCEL.code),
                    jsonPath("$.message").value(ErrorCode.ORDER_STATUS_INVALID_FOR_CANCEL.message(OrderStatus.CANCELLED.name)),
                )
            snippets = arrayOf(
                pathParameters(
                    parameterWithName("orderId").description("이미 취소된 주문 ID"),
                ),
                responseFields(commonErrorResponseSnippet())
            )
        }
    }

    @Test
    @WithMockAuthUser(userId = 3L)
    fun `주문 취소 - 실패 (다른 사용자의 주문)`() {
        val requestUserId = 3L
        val orderId = 10L

        val command = OrderCancelCommand(
            userId = requestUserId,
            orderId = orderId,
        )

        given(orderCommandUseCase.cancelOrder(eq(command)))
            .willThrow(OrderAccessDeniedException(orderId, requestUserId))

        performAndDocument("주문 취소 - 실패 (다른 사용자의 주문)") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/orders/{orderId}/cancel"
            urlVars = arrayOf(orderId)
            expectedStatus = status().isForbidden
            additionalMatchers =
                arrayOf(
                    jsonPath("$.code").value(ErrorCode.ORDER_ACCESS_DENIED.code),
                    jsonPath("$.message").value(ErrorCode.ORDER_ACCESS_DENIED.message(orderId, requestUserId)),
                )
            snippets =
                arrayOf(
                    pathParameters(
                        parameterWithName("orderId").description("다른 사용자의 주문 ID"),
                    ),
                    responseFields(commonErrorResponseSnippet()),
                )
        }
    }
}