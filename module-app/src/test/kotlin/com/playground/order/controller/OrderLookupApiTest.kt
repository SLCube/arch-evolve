//package com.playground.order.controller
//
//import com.playground.common.error.ErrorCode
//import com.playground.order.domain.enum.OrderStatus
//import com.playground.order.persistence.entity.OrderJpaEntity
//import com.playground.order.persistence.entity.OrderProductJpaEntity
//import com.playground.order.persistence.repository.OrderProductRepository
//import com.playground.order.persistence.repository.OrderRepository
//import com.playground.product.persistence.entity.ProductJpaEntity
//import com.playground.product.persistence.repository.ProductRepository
//import com.playground.support.ApiTest
//import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
//import com.playground.support.docs.performAndDocument
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.http.HttpMethod
//import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
//import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
//import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
//import org.springframework.restdocs.request.RequestDocumentation.pathParameters
//import org.springframework.restdocs.request.RequestDocumentation.queryParameters
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
//
//@Suppress("NonAsciiCharacters")
//class OrderLookupApiTest(
//    @param:Autowired private val productRepository: ProductRepository,
//    @param:Autowired private val orderRepository: OrderRepository,
//    @param:Autowired private val orderProductRepository: OrderProductRepository,
//) : ApiTest() {
//    @Test
//    fun `주문 상세 조회 - 성공`() {
//        val user = createUser("testUser", "password123", "테스트유저")
//        val productJpaEntity = productRepository.save(ProductJpaEntity(name = "상품1", stock = 10, price = 10000.toBigDecimal()))
//        val jwtToken = getAccessToken(user.loginId, "password123")
//
//        val orderJpaEntity =
//            orderRepository.save(
//                OrderJpaEntity(
//                    userId = user.id!!,
//                    totalPrice = productJpaEntity.price.multiply(2.toBigDecimal()),
//                    status = OrderStatus.PENDING,
//                ),
//            )
//        val orderProductJpaEntity =
//            orderProductRepository.save(
//                OrderProductJpaEntity(
//                    orderJpaEntity = orderJpaEntity,
//                    productId = productJpaEntity.id!!,
//                    quantity = 2,
//                    price = productJpaEntity.price,
//                ),
//            )
//        val orderId = orderJpaEntity.id!!
//
//        performAndDocument("주문 상세 조회 - 성공") {
//            httpMethod = HttpMethod.GET
//            urlTemplate = "/orders/{orderId}"
//            urlVars = arrayOf(orderId)
//            accessToken = jwtToken
//            expectedStatus = status().isOk
//            additionalMatchers =
//                arrayOf(
//                    jsonPath("$.id").value(orderId),
//                    jsonPath("$.userId").value(user.id),
//                    jsonPath("$.totalPrice").value(orderJpaEntity.totalPrice),
//                    jsonPath("$.status").value(OrderStatus.PENDING.name),
//                    jsonPath("$.orderProducts.length()").value(1),
//                    jsonPath("$.orderProducts[0].id").value(orderProductJpaEntity.id),
//                    jsonPath("$.orderProducts[0].productId").value(productJpaEntity.id),
//                    jsonPath("$.orderProducts[0].productName").value(productJpaEntity.name),
//                    jsonPath("$.orderProducts[0].quantity").value(orderProductJpaEntity.quantity),
//                    jsonPath("$.orderProducts[0].price").value(orderProductJpaEntity.price),
//                    jsonPath("$.createdAt").exists(),
//                )
//            snippets =
//                arrayOf(
//                    pathParameters(
//                        parameterWithName("orderId").description("조회할 주문 ID"),
//                    ),
//                    responseFields(
//                        fieldWithPath("id").description("주문 ID"),
//                        fieldWithPath("userId").description("주문 사용자 ID"),
//                        fieldWithPath("totalPrice").description("총 주문 금액"),
//                        fieldWithPath("status").description("주문 상태"),
//                        fieldWithPath("orderProducts[].id").description("주문 상품 ID"),
//                        fieldWithPath("orderProducts[].productId").description("상품 ID"),
//                        fieldWithPath("orderProducts[].productName").description("상품 이름"),
//                        fieldWithPath("orderProducts[].quantity").description("주문 수량"),
//                        fieldWithPath("orderProducts[].price").description("주문 당시 상품 단가"),
//                        fieldWithPath("createdAt").description("주문 생성일시"),
//                    ),
//                )
//        }
//    }
//
//    @Test
//    fun `주문 상세 조회 - 실패 (다른 사용자의 주문)`() {
//        val ownerUser = createUser("ownerUser", "password123", "주문소유자")
//        val otherUser = createUser("otherUser", "password123", "다른사용자")
//        val productJpaEntity = productRepository.save(ProductJpaEntity(name = "상품1", stock = 10, price = 10000.toBigDecimal()))
//
//        val orderJpaEntity =
//            orderRepository.save(
//                OrderJpaEntity(
//                    userId = ownerUser.id!!,
//                    totalPrice = productJpaEntity.price,
//                    status = OrderStatus.PENDING,
//                ),
//            )
//        orderProductRepository.save(
//            OrderProductJpaEntity(
//                orderJpaEntity = orderJpaEntity,
//                productId = productJpaEntity.id!!,
//                quantity = 1,
//                price = productJpaEntity.price,
//            ),
//        )
//        val orderId = orderJpaEntity.id!!
//
//        val otherUserJwtToken = getAccessToken(otherUser.loginId, "password123")
//        performAndDocument("주문 상세 조회 - 실패 (다른 사용자의 주문)") {
//            httpMethod = HttpMethod.GET
//            urlTemplate = "/orders/{orderId}"
//            urlVars = arrayOf(orderId)
//            accessToken = otherUserJwtToken
//            expectedStatus = status().isForbidden
//            additionalMatchers =
//                arrayOf(
//                    jsonPath("$.code").value(ErrorCode.ORDER_ACCESS_DENIED.code),
//                    jsonPath("$.message").value(ErrorCode.ORDER_ACCESS_DENIED.message(orderId, otherUser.id!!)),
//                )
//            snippets =
//                arrayOf(
//                    pathParameters(
//                        parameterWithName("orderId").description("다른 사용자의 주문 ID"),
//                    ),
//                    responseFields(commonErrorResponseSnippet()),
//                )
//        }
//    }
//
//    @Test
//    fun `주문 목록 조회 - 성공`() {
//        val user = createUser("testUser", "password123", "테스트유저")
//        val productJpaEntity1 = productRepository.save(ProductJpaEntity(name = "상품1", stock = 10, price = 10000.toBigDecimal()))
//        val productJpaEntity2 = productRepository.save(ProductJpaEntity(name = "상품2", stock = 5, price = 5000.toBigDecimal()))
//        val jwtToken = getAccessToken(user.loginId, "password123")
//
//        val orderJpaEntity1 =
//            orderRepository.save(
//                OrderJpaEntity(
//                    userId = requireNotNull(user.id),
//                    totalPrice = productJpaEntity1.price,
//                    status = OrderStatus.PENDING,
//                ),
//            )
//        orderProductRepository.save(
//            OrderProductJpaEntity(
//                orderJpaEntity = orderJpaEntity1,
//                productId = productJpaEntity1.id!!,
//                quantity = 1,
//                price = productJpaEntity1.price,
//            ),
//        )
//
//        val orderJpaEntity2 =
//            orderRepository.save(
//                OrderJpaEntity(
//                    userId = requireNotNull(user.id),
//                    totalPrice = productJpaEntity2.price.multiply(2.toBigDecimal()),
//                    status = OrderStatus.COMPLETED,
//                ),
//            )
//        orderProductRepository.save(
//            OrderProductJpaEntity(
//                orderJpaEntity = orderJpaEntity2,
//                productId = productJpaEntity2.id!!,
//                quantity = 2,
//                price = productJpaEntity2.price,
//            ),
//        )
//
//        performAndDocument("주문 목록 조회 - 성공") {
//            httpMethod = HttpMethod.GET
//            urlTemplate = "/orders"
//            accessToken = jwtToken
//            expectedStatus = status().isOk
//            additionalMatchers =
//                arrayOf(
//                    jsonPath("$.content.length()").value(2),
//                    jsonPath("$.content[0].id").value(orderJpaEntity2.id),
//                    jsonPath("$.content[0].representativeProductName").value(productJpaEntity2.name),
//                    jsonPath("$.content[0].totalPrice").value(orderJpaEntity2.totalPrice),
//                    jsonPath("$.content[0].status").value(OrderStatus.COMPLETED.name),
//                    jsonPath("$.content[1].id").value(orderJpaEntity1.id),
//                    jsonPath("$.content[1].representativeProductName").value(productJpaEntity1.name),
//                    jsonPath("$.content[1].totalPrice").value(orderJpaEntity1.totalPrice),
//                    jsonPath("$.content[1].status").value(OrderStatus.PENDING.name),
//                    jsonPath("$.pageNumber").value(0),
//                    jsonPath("$.pageSize").value(10),
//                    jsonPath("$.totalElements").value(2),
//                    jsonPath("$.totalPages").value(1),
//                )
//            snippets =
//                arrayOf(
//                    queryParameters(
//                        parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
//                        parameterWithName("size").description("페이지 크기").optional(),
//                        parameterWithName("sortBy").description("정렬 기준 필드 (예: createdAt)").optional(),
//                        parameterWithName("direction").description("정렬 방향 (ASC 또는 DESC)").optional(),
//                    ),
//                    responseFields(
//                        fieldWithPath("content[].id").description("주문 ID"),
//                        fieldWithPath("content[].representativeProductName").description("대표 상품명"),
//                        fieldWithPath("content[].totalPrice").description("총 주문 금액"),
//                        fieldWithPath("content[].status").description("주문 상태"),
//                        fieldWithPath("content[].createdAt").description("주문 생성일시"),
//                        fieldWithPath("pageNumber").description("현재 페이지 번호"),
//                        fieldWithPath("pageSize").description("페이지 크기"),
//                        fieldWithPath("totalElements").description("총 요소 개수"),
//                        fieldWithPath("totalPages").description("총 페이지 수"),
//                    ),
//                )
//        }
//    }
//
//    @Test
//    fun `주문 목록 조회 - 실패 (다른 사용자의 주문은 조회되지 않음)`() {
//        val ownerUser = createUser("ownerUser", "password123", "주문소유자")
//        val otherUser = createUser("otherUser", "password123", "다른사용자")
//        val productJpaEntity = productRepository.save(ProductJpaEntity(name = "상품1", stock = 10, price = 10000.toBigDecimal()))
//        val jwtToken = getAccessToken(otherUser.loginId, "password123")
//
//        val ownerOrder =
//            orderRepository.save(
//                OrderJpaEntity(
//                    userId = ownerUser.id!!,
//                    totalPrice = productJpaEntity.price,
//                    status = OrderStatus.PENDING,
//                ),
//            )
//        orderProductRepository.save(
//            OrderProductJpaEntity(
//                orderJpaEntity = ownerOrder,
//                productId = productJpaEntity.id!!,
//                quantity = 1,
//                price = productJpaEntity.price,
//            ),
//        )
//
//        performAndDocument("주문 목록 조회 - 실패 (다른 사용자의 주문은 조회되지 않음)") {
//            httpMethod = HttpMethod.GET
//            urlTemplate = "/orders"
//            accessToken = jwtToken
//            expectedStatus = status().isOk
//            additionalMatchers =
//                arrayOf(
//                    jsonPath("$.content.length()").value(0),
//                    jsonPath("$.pageNumber").value(0),
//                    jsonPath("$.pageSize").value(10),
//                    jsonPath("$.totalElements").value(0),
//                    jsonPath("$.totalPages").value(0),
//                )
//            snippets =
//                arrayOf(
//                    queryParameters(
//                        parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
//                        parameterWithName("size").description("페이지 크기").optional(),
//                        parameterWithName("sortBy").description("정렬 기준 필드 (예: createdAt)").optional(),
//                        parameterWithName("direction").description("정렬 방향 (ASC 또는 DESC)").optional(),
//                    ),
//                    responseFields(
//                        fieldWithPath("content").description("주문 목록 (비어있음)"),
//                        fieldWithPath("pageNumber").description("현재 페이지 번호"),
//                        fieldWithPath("pageSize").description("페이지 크기"),
//                        fieldWithPath("totalElements").description("총 요소 개수"),
//                        fieldWithPath("totalPages").description("총 페이지 수"),
//                    ),
//                )
//        }
//    }
//}
