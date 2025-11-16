package com.playground.order.controller

import com.playground.common.error.ErrorCode
import com.playground.order.domain.enum.OrderStatus
import com.playground.order.persistence.entity.OrderJpaEntity
import com.playground.order.persistence.entity.OrderProductJpaEntity
import com.playground.order.persistence.repository.OrderProductRepository
import com.playground.order.persistence.repository.OrderRepository
import com.playground.product.persistence.entity.ProductJpaEntity
import com.playground.product.persistence.repository.ProductRepository
import com.playground.support.ApiTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@WithMockUser
class OrderLookupApiTest(
    @param:Autowired private val productRepository: ProductRepository,
    @param:Autowired private val orderRepository: OrderRepository,
    @param:Autowired private val orderProductRepository: OrderProductRepository,
) : ApiTest() {
    @Test
    @WithMockUser(roles = ["USER"], username = "1")
    fun `주문 상세 조회 - 성공`() {
        val user = createUser("testUser", "password123", "테스트유저")
        val productJpaEntity = productRepository.save(ProductJpaEntity(name = "상품1", stock = 10, price = 10000L))
        val jwtToken = getAccessToken(user.loginId, "password123")

        val orderJpaEntity =
            orderRepository.save(
                OrderJpaEntity(
                    userId = user.id!!,
                    totalPrice = productJpaEntity.price * 2,
                    status = OrderStatus.PENDING,
                ),
            )
        val orderProductJpaEntity =
            orderProductRepository.save(
                OrderProductJpaEntity(
                    orderJpaEntity = orderJpaEntity,
                    productId = productJpaEntity.id!!,
                    quantity = 2,
                    price = productJpaEntity.price,
                ),
            )
        val orderId = orderJpaEntity.id!!

        performAndDocument("주문 상세 조회 - 성공") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/orders/{orderId}"
            urlVars = arrayOf(orderId)
            accessToken = jwtToken
            expectedStatus = status().isOk
            additionalMatchers =
                arrayOf(
                    jsonPath("$.id").value(orderId),
                    jsonPath("$.userId").value(user.id),
                    jsonPath("$.totalPrice").value(orderJpaEntity.totalPrice),
                    jsonPath("$.status").value(OrderStatus.PENDING.name),
                    jsonPath("$.orderProducts.length()").value(1),
                    jsonPath("$.orderProducts[0].id").value(orderProductJpaEntity.id),
                    jsonPath("$.orderProducts[0].productId").value(productJpaEntity.id),
                    jsonPath("$.orderProducts[0].productName").value(productJpaEntity.name),
                    jsonPath("$.orderProducts[0].quantity").value(orderProductJpaEntity.quantity),
                    jsonPath("$.orderProducts[0].price").value(orderProductJpaEntity.price),
                )
            snippets =
                arrayOf(
                    pathParameters(
                        parameterWithName("orderId").description("조회할 주문 ID"),
                    ),
                    responseFields(
                        fieldWithPath("id").description("주문 ID"),
                        fieldWithPath("userId").description("주문 사용자 ID"),
                        fieldWithPath("totalPrice").description("총 주문 금액"),
                        fieldWithPath("status").description("주문 상태"),
                        fieldWithPath("orderProducts[].id").description("주문 상품 ID"),
                        fieldWithPath("orderProducts[].productId").description("상품 ID"),
                        fieldWithPath("orderProducts[].productName").description("상품 이름"),
                        fieldWithPath("orderProducts[].quantity").description("주문 수량"),
                        fieldWithPath("orderProducts[].price").description("주문 당시 상품 단가"),
                    ),
                )
        }
    }

    @Test
    @WithMockUser(roles = ["USER"], username = "1")
    fun `주문 상세 조회 - 실패 (다른 사용자의 주문)`() {
        val ownerUser = createUser("ownerUser", "password123", "주문소유자")
        val otherUser = createUser("otherUser", "password123", "다른사용자")
        val productJpaEntity = productRepository.save(ProductJpaEntity(name = "상품1", stock = 10, price = 10000L))

        val orderJpaEntity =
            orderRepository.save(
                OrderJpaEntity(
                    userId = ownerUser.id!!,
                    totalPrice = productJpaEntity.price * 1,
                    status = OrderStatus.PENDING,
                ),
            )
        orderProductRepository.save(
            OrderProductJpaEntity(
                orderJpaEntity = orderJpaEntity,
                productId = productJpaEntity.id!!,
                quantity = 1,
                price = productJpaEntity.price,
            ),
        )
        val orderId = orderJpaEntity.id!!

        val otherUserJwtToken = getAccessToken(otherUser.loginId, "password123")
        performAndDocument("주문 상세 조회 - 실패 (다른 사용자의 주문)") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/orders/{orderId}"
            urlVars = arrayOf(orderId)
            accessToken = otherUserJwtToken
            expectedStatus = status().isForbidden
            additionalMatchers =
                arrayOf(
                    jsonPath("$.code").value(ErrorCode.ORDER_ACCESS_DENIED.code),
                    jsonPath("$.message").value(ErrorCode.ORDER_ACCESS_DENIED.message(orderId, otherUser.id!!)),
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
