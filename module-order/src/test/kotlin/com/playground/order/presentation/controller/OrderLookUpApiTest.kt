package com.playground.order.presentation.controller

import com.playground.common.error.ErrorCode
import com.playground.order.application.port.inbound.OrderQueryUseCase
import com.playground.order.application.service.result.OrderDetailResult
import com.playground.order.domain.exception.OrderAccessDeniedException
import com.playground.order.fixture.application.domain.DeliveryInfoTestFixture
import com.playground.order.fixture.application.domain.OrderDomainTestFixture
import com.playground.order.fixture.application.query.OrderQueryTestFixture
import com.playground.order.fixture.application.domain.ProductInfoTestFixture
import com.playground.order.presentation.annotation.OrderControllerSliceTest
import com.playground.support.RestDocsTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import com.playground.support.security.annotation.WithMockAuthUser
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@OrderControllerSliceTest
class OrderLookUpApiTest(
    @param:Autowired private val orderQueryUseCase: OrderQueryUseCase,
): RestDocsTest() {

    @Test
    @WithMockAuthUser(userId = 2L)
    fun `주문 상세 조회 - 성공`() {
        val mockOrder = OrderDomainTestFixture.mockOrder()
        val authenticatedUserId = 2L
        val mockDeliveryInfo = DeliveryInfoTestFixture.mockDeliveryInfo(orderId = mockOrder.id!!, userId = mockOrder.userId)
        val mockOrderDetailResult = OrderDetailResult.of(mockOrder, ProductInfoTestFixture.mockProductInfos(), mockDeliveryInfo)
        given(orderQueryUseCase.getOrder(eq(authenticatedUserId), eq(mockOrder.id!!)))
            .willReturn(mockOrderDetailResult)

        performAndDocument("주문 상세 조회 - 성공") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/orders/{orderId}"
            urlVars = arrayOf(mockOrder.id)
            expectedStatus = status().isOk
            additionalMatchers =
                arrayOf(
                    jsonPath("$.id").value(mockOrderDetailResult.id),
                    jsonPath("$.userId").value(mockOrderDetailResult.userId),
                    jsonPath("$.totalPrice").value(mockOrderDetailResult.totalPrice),
                    jsonPath("$.status").value(mockOrderDetailResult.status.name),
                    jsonPath("$.orderProducts.length()").value(mockOrderDetailResult.orderProducts.size),
                    jsonPath("$.orderProducts[0].id").value(mockOrderDetailResult.orderProducts[0].id),
                    jsonPath("$.orderProducts[0].productId").value(mockOrderDetailResult.orderProducts[0].productId),
                    jsonPath("$.orderProducts[0].productName").value(mockOrderDetailResult.orderProducts[0].productName),
                    jsonPath("$.orderProducts[0].quantity").value(mockOrderDetailResult.orderProducts[0].quantity),
                    jsonPath("$.orderProducts[0].price").value(mockOrderDetailResult.orderProducts[0].price),
                    jsonPath("$.orderProducts[1].id").value(mockOrderDetailResult.orderProducts[1].id),
                    jsonPath("$.orderProducts[1].productId").value(mockOrderDetailResult.orderProducts[1].productId),
                    jsonPath("$.orderProducts[1].productName").value(mockOrderDetailResult.orderProducts[1].productName),
                    jsonPath("$.orderProducts[1].quantity").value(mockOrderDetailResult.orderProducts[1].quantity),
                    jsonPath("$.orderProducts[1].price").value(mockOrderDetailResult.orderProducts[1].price),
                    jsonPath("$.delivery.deliveryId").value(mockOrderDetailResult.delivery.deliveryId),
                    jsonPath("$.delivery.receiverName").value(mockOrderDetailResult.delivery.receiverName),
                    jsonPath("$.delivery.receiverPhoneNumber").value(mockOrderDetailResult.delivery.receiverPhoneNumber),
                    jsonPath("$.delivery.zipCode").value(mockOrderDetailResult.delivery.zipCode),
                    jsonPath("$.delivery.baseAddress").value(mockOrderDetailResult.delivery.baseAddress),
                    jsonPath("$.delivery.detailAddress").value(mockOrderDetailResult.delivery.detailAddress),
                    jsonPath("$.delivery.deliveryStatus").value(mockOrderDetailResult.delivery.deliveryStatus),
                    jsonPath("$.createdAt").exists()
                )
            snippets =
                arrayOf(
                    pathParameters(
                        parameterWithName("orderId").description("주문 ID")
                    ),
                    responseFields(
                        fieldWithPath("id").description("주문 ID"),
                        fieldWithPath("userId").description("주문 사용자 ID"),
                        fieldWithPath("totalPrice").description("총 주문 금액"),
                        fieldWithPath("status").description("주문 상태"),
                        fieldWithPath("orderProducts[].id").description("주문 상품 ID"),
                        fieldWithPath("orderProducts[].productId").description("상품 ID"),
                        fieldWithPath("orderProducts[].productName").description("주문 상품 이름"),
                        fieldWithPath("orderProducts[].quantity").description("주문 상품 수량"),
                        fieldWithPath("orderProducts[].price").description("주문 상품 가격"),
                        fieldWithPath("delivery.deliveryId").description("배송 ID"),
                        fieldWithPath("delivery.receiverName").description("수령인 이름"),
                        fieldWithPath("delivery.receiverPhoneNumber").description("수령인 전화번호"),
                        fieldWithPath("delivery.zipCode").description("우편번호"),
                        fieldWithPath("delivery.baseAddress").description("기본 주소"),
                        fieldWithPath("delivery.detailAddress").description("상세 주소"),
                        fieldWithPath("delivery.deliveryStatus").description("배송 상태"),
                        fieldWithPath("createdAt").description("주문 생성 시간")
                    )
                )
        }
    }

    @Test
    @WithMockAuthUser(userId = 3L)
    fun `주문 상세 조회 - 실패(다른 사용자의 주문)`() {
        val requestUserId = 3L
        val requestOrderId = 10L

        given(orderQueryUseCase.getOrder(eq(requestUserId), eq(requestOrderId)))
            .willThrow(OrderAccessDeniedException(requestOrderId, requestUserId))

        performAndDocument("주문 상세 조회 - 실패(다른 사용자의 주문") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/orders/{orderId}"
            urlVars = arrayOf(requestOrderId)
            expectedStatus = status().isForbidden
            additionalMatchers =
                arrayOf(
                    jsonPath("$.code").value(ErrorCode.ORDER_ACCESS_DENIED.code),
                    jsonPath("$.message").value(ErrorCode.ORDER_ACCESS_DENIED.message(requestOrderId, requestUserId))
                )
            snippets =
                arrayOf(
                    pathParameters(
                        parameterWithName("orderId").description("주문 ID")
                    ),
                    responseFields(commonErrorResponseSnippet())
                )
        }
    }

    @Test
    @WithMockAuthUser
    fun `주문 목록 조회 - 성공`() {
        val authenticatedUserId = 2L
        val mockOrders = OrderQueryTestFixture.mockOrderSummaryPage()

        given(orderQueryUseCase.getOrders(eq(authenticatedUserId), any()))
            .willReturn(mockOrders)

        performAndDocument("주문 목록 조회 - 성공") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/orders"
            expectedStatus = status().isOk
            additionalMatchers =
                arrayOf(
                    jsonPath("$.content.length()").value(mockOrders.content.size),
                    jsonPath("$.content[0].id").value(mockOrders.content[0].id),
                    jsonPath("$.content[0].representativeProductName").value(mockOrders.content[0].representativeProductName),
                    jsonPath("$.content[0].totalPrice").value(mockOrders.content[0].totalPrice),
                    jsonPath("$.content[0].status").value(mockOrders.content[0].status.name),
                    jsonPath("$.content[0].createdAt").exists(),
                    jsonPath("$.content[1].id").value(mockOrders.content[1].id),
                    jsonPath("$.content[1].representativeProductName").value(mockOrders.content[1].representativeProductName),
                    jsonPath("$.content[1].totalPrice").value(mockOrders.content[1].totalPrice),
                    jsonPath("$.content[1].status").value(mockOrders.content[1].status.name),
                    jsonPath("$.content[1].createdAt").exists(),
                    jsonPath("$.pageNumber").value(mockOrders.pageNumber),
                    jsonPath("$.pageSize").value(mockOrders.pageSize),
                    jsonPath("$.totalElements").value(mockOrders.totalElements),
                    jsonPath("$.totalPages").value(mockOrders.totalPages),
                )
            snippets =
                arrayOf(
                    queryParameters(
                        parameterWithName("page").description("페이지 번호").optional(),
                        parameterWithName("size").description("페이지 크기").optional(),
                        parameterWithName("sortBy").description("정렬 기준").optional(),
                        parameterWithName("direction").description("정렬 방향(ASC 또는 DESC").optional()
                    ),
                    responseFields(
                        fieldWithPath("content[].id").description("주문 ID"),
                        fieldWithPath("content[].representativeProductName").description("주문 상품 이름"),
                        fieldWithPath("content[].totalPrice").description("총 주문 금액"),
                        fieldWithPath("content[].status").description("주문 상태"),
                        fieldWithPath("content[].createdAt").description("주문 생성 일자"),
                        fieldWithPath("pageNumber").description("현재 페이지 번호"),
                        fieldWithPath("pageSize").description("페이지 크기"),
                        fieldWithPath("totalElements").description("총 요소 수"),
                        fieldWithPath("totalPages").description("총 페이지 수"),
                    )
                )
        }
    }

    @Test
    @WithMockAuthUser(userId = 3L)
    fun `주문 목록 조회 - 실패 (다른 사용자의 주문은 조회되지 않음)`() {
        val requestUserId = 3L
        val emptyOrder = OrderQueryTestFixture.mockEmptyOrderSummaryPage()

        given(orderQueryUseCase.getOrders(eq(requestUserId), any()))
            .willReturn(emptyOrder)

        performAndDocument("주문 목록 조회 - 실패 (다른 사용자의 주문은 조회되지 않음)") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/orders"
            expectedStatus = status().isOk
            additionalMatchers =
                arrayOf(
                    jsonPath("$.content.length()").value(0),
                    jsonPath("$.pageNumber").value(emptyOrder.pageNumber),
                    jsonPath("$.pageSize").value(emptyOrder.pageSize),
                    jsonPath("$.totalElements").value(0),
                    jsonPath("$.totalPages").value(0)
                )
            snippets =
                arrayOf(
                    queryParameters(
                        parameterWithName("page").description("페이지 번호").optional(),
                        parameterWithName("size").description("페이지 크기").optional(),
                        parameterWithName("sortBy").description("정렬 기준").optional(),
                        parameterWithName("direction").description("정렬 방향 (ASC 또는 DESC)").optional()
                    ),
                    responseFields(
                        fieldWithPath("content").description("주문 목록 (비어있음)"),
                        fieldWithPath("pageNumber").description("현재 페이지 번호"),
                        fieldWithPath("pageSize").description("페이지 크기"),
                        fieldWithPath("totalElements").description("총 요소 수"),
                        fieldWithPath("totalPages").description("총 페이지 수"),
                    )
                )
        }
    }
}
