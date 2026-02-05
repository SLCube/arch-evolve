package com.playground.order.presentation.controller

import com.playground.common.error.ErrorCode
import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.domain.exception.OrderableProductNotFoundException
import com.playground.order.fixture.presentation.request.OrderRequestTestFixture
import com.playground.order.fixture.application.domain.OrderDomainTestFixture
import com.playground.order.presentation.annotation.OrderControllerSliceTest
import com.playground.order.presentation.request.OrderProductRequestDto
import com.playground.support.RestDocsTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import com.playground.support.security.annotation.WithMockAuthUser
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@OrderControllerSliceTest
class OrderSaveApiTest(
    @param:Autowired private val orderCommandUseCase: OrderCommandUseCase,
) : RestDocsTest() {

    @Test
    @WithMockAuthUser
    fun `주문 생성 - 성공`() {
        val orderCreateRequestDto = OrderRequestTestFixture.createOrderRequest()

        val mockOrder = OrderDomainTestFixture.mockOrder()

        given(orderCommandUseCase.createOrder(any()))
            .willReturn(mockOrder)

        performAndDocument("주문 생성 - 성공") {
            tag = "주문 API"
            summary = "주문 생성"
            description = "사용자가 여러 상품을 선택하여 주문을 생성합니다. 주문 생성 시 상품 ID와 수량, 배송지 ID를 전달하면 주문 정보가 생성됩니다."

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
            snippets =
                arrayOf(
                    requestFields(
                        fieldWithPath("addressId").description("주소지 ID"),
                        fieldWithPath("orderProducts[].productId").description("주문 상품 ID"),
                        fieldWithPath("orderProducts[].quantity").description("주문 수량"),
                    ),
                    responseFields(
                        fieldWithPath("id").description("주문 ID"),
                        fieldWithPath("userId").description("주문 사용자 ID"),
                        fieldWithPath("totalPrice").description("총 주문 금액"),
                        fieldWithPath("status").description("주문 상태"),
                        fieldWithPath("orderProducts[].id").description("주문 상품 ID"),
                        fieldWithPath("orderProducts[].productId").description("상품 ID"),
                        fieldWithPath("orderProducts[].quantity").description("주문 수량"),
                        fieldWithPath("orderProducts[].price").description("주문 당시 상품 단가"),
                    )
                )
        }
    }

    @Test
    @WithMockAuthUser
    fun `주문 생성 - 실패, 상품이 존재하지 않음`() {
        val nonExistingProductId = 999L

        val request = OrderRequestTestFixture.createOrderRequest(
            orderProducts = listOf(
                OrderProductRequestDto(productId = nonExistingProductId, quantity = 1),
            )
        )

        given(orderCommandUseCase.createOrder(any()))
            .willThrow(OrderableProductNotFoundException(nonExistingProductId))

        performAndDocument("주문 생성 - 실패, 상품이 존재하지 않음") {
            tag = "주문 API"
            summary = "주문 생성 실패 - 존재하지 않는 상품"
            description = "존재하지 않는 상품 ID로 주문 생성을 시도할 경우 404 Not Found 에러가 발생합니다."

            httpMethod = HttpMethod.POST
            urlTemplate = "/orders"
            requestBody = request
            expectedStatus = status().isNotFound
            additionalMatchers =
                arrayOf(
                    jsonPath("$.code").value(ErrorCode.ORDERABLE_PRODUCT_NOT_FOUND.code),
                    jsonPath("$.message").value(ErrorCode.ORDERABLE_PRODUCT_NOT_FOUND.message(nonExistingProductId))
                )
            snippets =
                arrayOf(
                    responseFields(commonErrorResponseSnippet())
                )
        }
    }

    @Test
    @WithMockAuthUser
    fun `주문 생성 - 실패, 주문 상품이 비어있음`() {
        val request = OrderRequestTestFixture.createOrderRequest(
            orderProducts = mutableListOf()
        )

        performAndDocument("주문 생성 - 실패, 주문 상품이 비어있음") {
            tag = "주문 API"
            summary = "주문 생성 실패 - 빈 상품 목록"
            description = "주문 상품 목록이 비어있는 경우 400 Bad Request 에러가 발생합니다. 주문 생성 시 최소 1개 이상의 상품이 필요합니다."

            httpMethod = HttpMethod.POST
            urlTemplate = "/orders"
            requestBody = request
            expectedStatus = status().isBadRequest
            additionalMatchers =
                arrayOf(
                    jsonPath("$.message").value("입력값이 유효하지 않습니다."),
                    jsonPath("$.errors.orderProducts").value("주문 상품은 최소 1개 이상이어야 합니다."),
                )
            snippets =
                arrayOf(
                    responseFields(
                        commonErrorResponseSnippet() +
                                fieldWithPath("errors.orderProducts").description("주문 상품 목록 필드의 에러 메세지")
                    )
                )
        }
    }
}