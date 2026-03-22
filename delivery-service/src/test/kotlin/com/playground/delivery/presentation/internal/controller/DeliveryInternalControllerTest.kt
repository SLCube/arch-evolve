package com.playground.delivery.presentation.internal.controller

import com.playground.delivery.application.port.inbound.DeliveryQueryUseCase
import com.playground.delivery.domain.exception.DeliveryNotFoundException
import com.playground.delivery.fixture.application.domain.DeliveryDomainTestFixture
import com.playground.delivery.presentation.internal.annotation.DeliveryInternalControllerSliceTest
import com.playground.support.RestDocsTest
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.Test
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
@DeliveryInternalControllerSliceTest
class DeliveryInternalControllerTest(
    @param:Autowired private val deliveryQueryUseCase: DeliveryQueryUseCase,
) : RestDocsTest() {
    @Test
    fun `배송 내부 조회 - 성공`() {
        // given
        val delivery =
            DeliveryDomainTestFixture.mockDelivery(
                id = 1L,
                orderId = 100L,
                userId = 3L,
            )

        given(deliveryQueryUseCase.getDeliveryByOrderId(100L))
            .willReturn(delivery)

        // when / then
        performAndDocument("배송 내부 조회 - 성공") {
            tag = "배송 내부 API"
            summary = "주문 ID로 배송 정보 조회 (internal)"
            description = "서비스 간 통신용 internal API. 주문 ID에 해당하는 배송 정보를 조회합니다."
            httpMethod = HttpMethod.GET
            urlTemplate = "/internal/deliveries/{orderId}"
            urlVars = arrayOf(100L)
            expectedStatus = status().isOk
            additionalMatchers =
                arrayOf(
                    jsonPath("$.deliveryId").value(delivery.id),
                    jsonPath("$.orderId").value(delivery.orderId),
                    jsonPath("$.userId").value(delivery.userId),
                    jsonPath("$.receiverName").value(delivery.deliveryReceiver.receiverName),
                    jsonPath("$.receiverPhoneNumber").value(delivery.deliveryReceiver.receiverPhoneNumber),
                    jsonPath("$.zipCode").value(delivery.deliveryAddress.zipCode),
                    jsonPath("$.baseAddress").value(delivery.deliveryAddress.baseAddress),
                    jsonPath("$.detailAddress").value(delivery.deliveryAddress.detailAddress),
                    jsonPath("$.deliveryStatus").value(delivery.deliveryStatus.name),
                )
            snippets =
                arrayOf(
                    pathParameters(
                        parameterWithName("orderId").description("주문 ID"),
                    ),
                    responseFields(
                        fieldWithPath("deliveryId").description("배송 ID"),
                        fieldWithPath("orderId").description("주문 ID"),
                        fieldWithPath("userId").description("사용자 ID"),
                        fieldWithPath("receiverName").description("수령인 이름"),
                        fieldWithPath("receiverPhoneNumber").description("수령인 전화번호"),
                        fieldWithPath("zipCode").description("우편번호"),
                        fieldWithPath("baseAddress").description("기본 주소"),
                        fieldWithPath("detailAddress").description("상세 주소"),
                        fieldWithPath("deliveryStatus").description("배송 상태"),
                    ),
                )
        }
    }

    @Test
    fun `배송 내부 조회 - 존재하지 않는 orderId 조회 시 404 반환`() {
        // given
        given(deliveryQueryUseCase.getDeliveryByOrderId(9999L))
            .willThrow(DeliveryNotFoundException(9999L))

        // when / then
        performAndDocument("배송 내부 조회 - 404 Not Found") {
            tag = "배송 내부 API"
            summary = "주문 ID로 배송 정보 조회 (internal)"
            description = "서비스 간 통신용 internal API. 주문 ID에 해당하는 배송 정보를 조회합니다."
            httpMethod = HttpMethod.GET
            urlTemplate = "/internal/deliveries/{orderId}"
            urlVars = arrayOf(9999L)
            expectedStatus = status().isNotFound
            additionalMatchers =
                arrayOf(
                    jsonPath("$.code").value("DELIVERY_NOT_FOUND"),
                )
            snippets =
                arrayOf(
                    pathParameters(
                        parameterWithName("orderId").description("주문 ID"),
                    ),
                )
        }
    }
}
