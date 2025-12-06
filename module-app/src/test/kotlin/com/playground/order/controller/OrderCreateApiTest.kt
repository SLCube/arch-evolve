//package com.playground.order.controller
//
//import com.playground.common.error.ErrorCode
//import com.playground.order.presentation.request.OrderCreateRequestDto
//import com.playground.order.presentation.request.OrderProductRequestDto
//import com.playground.payment.persistence.entity.PaymentMethodJpaEntity
//import com.playground.payment.persistence.repository.PaymentMethodRepository
//import com.playground.product.persistence.entity.ProductJpaEntity
//import com.playground.product.persistence.repository.ProductRepository
//import com.playground.support.ApiTest
//import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
//import com.playground.support.docs.performAndDocument
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.http.HttpMethod
//import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
//import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
//import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
//import org.springframework.security.test.context.support.WithMockUser
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
//import java.util.*
//
//@Suppress("NonAsciiCharacters")
//class OrderCreateApiTest(
//    @param:Autowired private val productRepository: ProductRepository,
//    @param:Autowired private val paymentMethodRepository: PaymentMethodRepository,
//) : ApiTest() {
//    @Test
//    fun `주문 생성 - 실패, 상품이 존재하지 않음`() {
//        val user = createUser("testUser", "password123", "테스트유저")
//        val nonExistingProductId = 999L
//
//        val orderRequest =
//            OrderCreateRequestDto(
//                // todo -> 주소지아이디 임시로 입력
//                addressId = 1L,
//                orderProducts =
//                    listOf(
//                        OrderProductRequestDto(productId = nonExistingProductId, quantity = 1),
//                    ),
//            )
//        val jwtToken = getAccessToken(user.loginId, "password123")
//
//        performAndDocument("주문 생성 - 실패, 상품이 존재하지 않음") {
//            httpMethod = HttpMethod.POST
//            urlTemplate = "/orders"
//            requestBody = orderRequest
//            accessToken = jwtToken
//            expectedStatus = status().isNotFound
//            additionalMatchers =
//                arrayOf(
//                    jsonPath("$.code").value(ErrorCode.ORDERABLE_PRODUCT_NOT_FOUND.code),
//                    jsonPath("$.message").value(ErrorCode.ORDERABLE_PRODUCT_NOT_FOUND.message(nonExistingProductId)),
//                )
//            snippets =
//                arrayOf(
//                    responseFields(commonErrorResponseSnippet()),
//                )
//        }
//    }
//
//    @Test
//    @WithMockUser(roles = ["USER"], username = "1")
//    fun `주문 생성 - 실패, 재고 부족`() {
//        val user = createUser("testUser", "password123", "테스트유저")
//        val productJpaEntity = productRepository.save(ProductJpaEntity(name = "상품1", stock = 5, price = 10000.toBigDecimal()))
//
//        val orderRequest =
//            OrderCreateRequestDto(
//                // todo -> 주소지아이디 임시로 입력
//                addressId = 1L,
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
//
//    @Test
//    fun `주문 생성 - 실패, 주문 상품이 비어있음`() {
//        val user = createUser("testUser", "password123", "테스트유저")
//
//        val orderRequest =
//            OrderCreateRequestDto(
//                // todo -> 주소지아이디 임시로 입력
//                addressId = 1L,
//                orderProducts = listOf(), // 비어있는 주문 상품 목록
//            )
//        val jwtToken = getAccessToken(user.loginId, "password123")
//
//        performAndDocument("주문 생성 - 실패, 주문 상품이 비어있음") {
//            httpMethod = HttpMethod.POST
//            urlTemplate = "/orders"
//            requestBody = orderRequest
//            accessToken = jwtToken
//            expectedStatus = status().isBadRequest
//            additionalMatchers =
//                arrayOf(
//                    jsonPath("$.message").value("입력값이 유효하지 않습니다."),
//                    jsonPath("$.errors.orderProducts").value("주문 상품은 최소 1개 이상이어야 합니다."),
//                )
//            snippets =
//                arrayOf(
//                    responseFields(
//                        commonErrorResponseSnippet() +
//                            fieldWithPath("errors.orderProducts").description("주문 상품 목록 필드의 에러 메시지"),
//                    ),
//                )
//        }
//    }
//
//    @Test
//    fun `주문 생성 - 실패, 주문 수량이 1개 미만`() {
//        val user = createUser("testUser", "password123", "테스트유저")
//        val productJpaEntity = productRepository.save(ProductJpaEntity(name = "상품1", stock = 10, price = 10000.toBigDecimal()))
//        val orderRequest =
//            OrderCreateRequestDto(
//                // todo -> 주소지아이디 임시로 입력
//                addressId = 1L,
//                orderProducts =
//                    listOf(
//                        OrderProductRequestDto(productId = productJpaEntity.id!!, quantity = 0), // 1개 미만 수량
//                    ),
//            )
//        val jwtToken = getAccessToken(user.loginId, "password123")
//
//        performAndDocument("주문 생성 - 실패, 주문 수량이 1개 미만") {
//            httpMethod = HttpMethod.POST
//            urlTemplate = "/orders"
//            requestBody = orderRequest
//            accessToken = jwtToken
//            expectedStatus = status().isBadRequest
//            additionalMatchers =
//                arrayOf(
//                    jsonPath("$.message").value("입력값이 유효하지 않습니다."),
//                    jsonPath("$.errors.['orderProducts[0].quantity']").value("주문 수량은 1개 이상이어야 합니다."),
//                )
//            snippets =
//                arrayOf(
//                    responseFields(
//                        commonErrorResponseSnippet() +
//                            fieldWithPath("errors.['orderProducts[0].quantity']").description("주문 수량 필드의 에러 메시지"),
//                    ),
//                )
//        }
//    }
//}
