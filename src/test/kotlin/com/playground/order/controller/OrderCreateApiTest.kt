package com.playground.order.controller

import com.playground.common.error.ErrorCode
import com.playground.order.presentation.request.OrderCreateRequestDto
import com.playground.order.presentation.request.OrderProductRequestDto
import com.playground.product.persistence.entity.ProductJpaEntity
import com.playground.product.persistence.repository.ProductRepository
import com.playground.support.ApiTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@WithMockUser
class OrderCreateApiTest(
    @param:Autowired private val productRepository: ProductRepository,
) : ApiTest() {
    @Test
    @WithMockUser(roles = ["USER"], username = "1")
    fun `주문 생성 - 성공`() {
        val user = createUser("testUser", "password123", "테스트유저")
        val productJpaEntity1 = productRepository.save(ProductJpaEntity(name = "상품1", stock = 10, price = 10000L))
        val productJpaEntity2 = productRepository.save(ProductJpaEntity(name = "상품2", stock = 5, price = 5000L))

        val orderRequest =
            OrderCreateRequestDto(
                orderProducts =
                    listOf(
                        OrderProductRequestDto(productId = productJpaEntity1.id!!, quantity = 2),
                        OrderProductRequestDto(productId = productJpaEntity2.id!!, quantity = 3),
                    ),
            )

        val jwtToken = getAccessToken(user.loginId, "password123")

        performAndDocument("주문 생성 - 성공") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/orders"
            requestBody = orderRequest
            accessToken = jwtToken
            expectedStatus = status().isCreated
            additionalMatchers =
                arrayOf(
                    jsonPath("$.userId").value(user.id),
                    jsonPath("$.totalPrice").value(productJpaEntity1.price * 2 + productJpaEntity2.price * 3),
                    jsonPath("$.status").value("PENDING"),
                    jsonPath("$.orderProducts.length()").value(2),
                    jsonPath("$.orderProducts[0].productId").value(productJpaEntity1.id),
                    jsonPath("$.orderProducts[0].quantity").value(2),
                    jsonPath("$.orderProducts[0].price").value(productJpaEntity1.price),
                    jsonPath("$.orderProducts[1].productId").value(productJpaEntity2.id),
                    jsonPath("$.orderProducts[1].quantity").value(3),
                    jsonPath("$.orderProducts[1].price").value(productJpaEntity2.price),
                )
            snippets =
                arrayOf(
                    requestFields(
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
                    ),
                )
        }
    }

    @Test
    @WithMockUser(roles = ["USER"], username = "1")
    fun `주문 생성 - 실패, 상품이 존재하지 않음`() {
        val user = createUser("testUser", "password123", "테스트유저")
        val nonExistingProductId = 999L

        val orderRequest =
            OrderCreateRequestDto(
                orderProducts =
                    listOf(
                        OrderProductRequestDto(productId = nonExistingProductId, quantity = 1),
                    ),
            )
        val jwtToken = getAccessToken(user.loginId, "password123")

        performAndDocument("주문 생성 - 실패, 상품이 존재하지 않음") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/orders"
            requestBody = orderRequest
            accessToken = jwtToken
            expectedStatus = status().isNotFound
            additionalMatchers =
                arrayOf(
                    jsonPath("$.code").value(ErrorCode.ORDERABLE_PRODUCT_NOT_FOUND.code),
                    jsonPath("$.message").value(ErrorCode.ORDERABLE_PRODUCT_NOT_FOUND.message(nonExistingProductId)),
                )
            snippets =
                arrayOf(
                    responseFields(commonErrorResponseSnippet()),
                )
        }
    }

    // todo -> 멀티모듈 설정 후 테스트 다시 활성화
//    @Test
//    @WithMockUser(roles = ["USER"], username = "1")
//    fun `주문 생성 - 실패, 재고 부족`() {
//        val user = createUser("testUser", "password123", "테스트유저")
//        val productJpaEntity = productRepository.save(ProductJpaEntity(name = "상품1", stock = 5, price = 10000L))
//
//        val orderRequest =
//            OrderCreateRequestDto(
//                orderProducts =
//                    listOf(
//                        OrderProductRequestDto(productId = productJpaEntity.id!!, quantity = 10), // 재고보다 많은 수량
//                    ),
//            )
//        val jwtToken = getAccessToken(user.loginId, "password123")
//
//        performAndDocument("주문 생성 - 실패, 재고 부족") {
//            httpMethod = HttpMethod.POST
//            urlTemplate = "/orders"
//            requestBody = orderRequest
//            accessToken = jwtToken
//            expectedStatus = status().isBadRequest
//            additionalMatchers =
//                arrayOf(
//                    jsonPath("$.code").value(ErrorCode.INSUFFICIENT_STOCK.code),
//                    jsonPath("$.message").value(
//                        ErrorCode.INSUFFICIENT_STOCK.message(
//                            productJpaEntity.id,
//                            productJpaEntity.stock,
//                            10,
//                        ),
//                    ),
//                )
//            snippets =
//                arrayOf(
//                    responseFields(commonErrorResponseSnippet()),
//                )
//        }
//    }

    @Test
    @WithMockUser(roles = ["USER"], username = "1")
    fun `주문 생성 - 실패, 주문 상품이 비어있음`() {
        val user = createUser("testUser", "password123", "테스트유저")

        val orderRequest =
            OrderCreateRequestDto(
                orderProducts = listOf(), // 비어있는 주문 상품 목록
            )
        val jwtToken = getAccessToken(user.loginId, "password123")

        performAndDocument("주문 생성 - 실패, 주문 상품이 비어있음") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/orders"
            requestBody = orderRequest
            accessToken = jwtToken
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
                            fieldWithPath("errors.orderProducts").description("주문 상품 목록 필드의 에러 메시지"),
                    ),
                )
        }
    }

    @Test
    @WithMockUser(roles = ["USER"], username = "1")
    fun `주문 생성 - 실패, 주문 수량이 1개 미만`() {
        val user = createUser("testUser", "password123", "테스트유저")
        val productJpaEntity = productRepository.save(ProductJpaEntity(name = "상품1", stock = 10, price = 10000L))

        val orderRequest =
            OrderCreateRequestDto(
                orderProducts =
                    listOf(
                        OrderProductRequestDto(productId = productJpaEntity.id!!, quantity = 0), // 1개 미만 수량
                    ),
            )
        val jwtToken = getAccessToken(user.loginId, "password123")

        performAndDocument("주문 생성 - 실패, 주문 수량이 1개 미만") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/orders"
            requestBody = orderRequest
            accessToken = jwtToken
            expectedStatus = status().isBadRequest
            additionalMatchers =
                arrayOf(
                    jsonPath("$.message").value("입력값이 유효하지 않습니다."),
                    jsonPath("$.errors.['orderProducts[0].quantity']").value("주문 수량은 1개 이상이어야 합니다."),
                )
            snippets =
                arrayOf(
                    responseFields(
                        commonErrorResponseSnippet() +
                            fieldWithPath("errors.['orderProducts[0].quantity']").description("주문 수량 필드의 에러 메시지"),
                    ),
                )
        }
    }
}
