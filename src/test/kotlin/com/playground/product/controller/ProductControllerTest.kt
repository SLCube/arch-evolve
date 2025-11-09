package com.playground.product.controller

import com.playground.common.error.ErrorCode
import com.playground.product.controller.request.ProductSaveRequestDto
import com.playground.product.controller.request.ProductUpdateRequestDto
import com.playground.product.domain.Product
import com.playground.product.repository.ProductRepository
import com.playground.support.ApiTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.queryParameters
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@WithMockUser
class ProductControllerTest(
    @param:Autowired private val productRepository: ProductRepository
) : ApiTest() {

    @AfterEach
    fun cleanUpProduct() {
        productRepository.deleteAll()
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `상품 등록 - 성공`() {
        val request = ProductSaveRequestDto(
            name = "상품1",
            stock = 10
        )

        performAndDocument("상품 등록 - 성공") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/products"
            requestBody = request
            expectedStatus = status().isCreated
            additionalMatchers = arrayOf(
                jsonPath("$.name").value("상품1"),
                jsonPath("$.stock").value(10)
            )
            snippets = arrayOf(
                requestFields(
                    fieldWithPath("name").description("상품 이름"),
                    fieldWithPath("stock").description("재고량")
                ),
                responseFields(
                    fieldWithPath("id").description("상품 ID"),
                    fieldWithPath("name").description("상품 이름"),
                    fieldWithPath("stock").description("재고량")
                )
            )
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `상품 등록 - 실패, 이름이 비어있음`() {
        val request = ProductSaveRequestDto(
            name = "",
            stock = 10
        )

        performAndDocument("상품 등록 - 실패, 이름이 비어있음") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/products"
            requestBody = request
            expectedStatus = status().isBadRequest
            additionalMatchers = arrayOf(
                jsonPath("$.message").value("입력값이 유효하지 않습니다."),
                jsonPath("$.errors.name").value("상품 이름은 필수입니다.")
            )
            snippets = arrayOf(
                responseFields(
                    commonErrorResponseSnippet() +
                            fieldWithPath("errors.name").description("상품 이름 필드의 에러 메시지")
                )
            )
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `상품 등록 - 실패, 재고가 0보다 작음`() {
        val request = ProductSaveRequestDto(
            name = "상품1",
            stock = -1
        )

        performAndDocument("상품 등록 - 실패, 재고가 0보다 작음") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/products"
            requestBody = request
            expectedStatus = status().isBadRequest
            additionalMatchers = arrayOf(
                jsonPath("$.message").value("입력값이 유효하지 않습니다."),
                jsonPath("$.errors.stock").value("재고량은 0보다 커야 합니다.")
            )
            snippets = arrayOf(
                responseFields(
                    commonErrorResponseSnippet() +
                            fieldWithPath("errors.stock").description("재고량 필드의 에러 메시지")
                )
            )
        }
    }

    @Test
    fun `상품 단일 조회 - 성공`() {
        val savedProduct = productRepository.save(Product(name = "상품1", stock = 10))

        performAndDocument("상품 단일 조회 - 성공") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/products/{id}"
            urlVars = arrayOf(savedProduct.id)
            expectedStatus = status().isOk
            additionalMatchers = arrayOf(
                jsonPath("$.id").value(savedProduct.id),
                jsonPath("$.name").value(savedProduct.name),
                jsonPath("$.stock").value(savedProduct.stock)
            )
            snippets = arrayOf(
                responseFields(
                    fieldWithPath("id").description("상품 ID"),
                    fieldWithPath("name").description("상품 이름"),
                    fieldWithPath("stock").description("재고량")
                )
            )
        }
    }

    @Test
    fun `상품 단일 조회 - 실패, 존재하지 않는 상품`() {
        val nonExistingId = 999L

        performAndDocument("상품 단일 조회 - 실패, 존재하지 않는 상품") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/products/{id}"
            urlVars = arrayOf(nonExistingId)
            expectedStatus = status().isNotFound
            additionalMatchers = arrayOf(
                jsonPath("$.code").value(ErrorCode.PRODUCT_NOT_FOUND.code),
                jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.message(nonExistingId))
            )
            snippets = arrayOf(
                responseFields(commonErrorResponseSnippet())
            )
        }
    }

    @Test
    fun `상품 목록 조회 - 성공`() {
        productRepository.save(Product(name = "상품1", stock = 10))
        productRepository.save(Product(name = "상품2", stock = 20))

        performAndDocument("상품 목록 조회 - 성공") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/products"
            expectedStatus = status().isOk
            additionalMatchers = arrayOf(
                jsonPath("$.length()").value(2)
            )
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `상품 수정 - 성공`() {
        val savedProduct = productRepository.save(Product(name = "상품1", stock = 10))
        val updateRequest = ProductUpdateRequestDto(name = "상품2", stock = 20)

        performAndDocument("상품 수정 - 성공") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/products/{id}"
            urlVars = arrayOf(savedProduct.id)
            requestBody = updateRequest
            expectedStatus = status().isOk
            additionalMatchers = arrayOf(
                jsonPath("$.name").value("상품2"),
                jsonPath("$.stock").value(20)
            )
            snippets = arrayOf(
                requestFields(
                    fieldWithPath("name").description("상품 이름"),
                    fieldWithPath("stock").description("재고량")
                ),
                responseFields(
                    fieldWithPath("id").description("상품 ID"),
                    fieldWithPath("name").description("상품 이름"),
                    fieldWithPath("stock").description("재고량")
                )
            )
        }
    }

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `재고 차감 - 실패, 재고 부족`() {
        val savedProduct = productRepository.save(Product(name = "테스트 상품", stock = 10))
        val quantity = 11

        performAndDocument("재고 차감 - 실패, 재고 부족") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/products/{id}/decrease-stock"
            urlVars = arrayOf(savedProduct.id)
            queryParams {
                add("quantity", quantity.toString())
            }
            expectedStatus = status().isBadRequest
            additionalMatchers = arrayOf(
                jsonPath("$.code").value(ErrorCode.INSUFFICIENT_STOCK.code),
                jsonPath("$.message").value(
                    ErrorCode.INSUFFICIENT_STOCK.message(
                        savedProduct.id,
                        savedProduct.stock,
                        quantity
                    )
                )
            )
            snippets = arrayOf(
                queryParameters(
                    parameterWithName("quantity").description("차감할 재고 수량")
                ),
                responseFields(commonErrorResponseSnippet())
            )
        }
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `상품 등록 - 실패, USER 권한으로 ADMIN API 접근 시도`() {
        val request = ProductSaveRequestDto(name = "상품1", stock = 10)

        performAndDocument("상품 등록 - 실패, USER 권한으로 ADMIN API 접근 시도") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/products"
            requestBody = request
            expectedStatus = status().isForbidden // 403 Forbidden 기대
            snippets = arrayOf(
                responseFields(commonErrorResponseSnippet())
            )
        }
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `상품 수정 - 실패, USER 권한으로 ADMIN API 접근 시도`() {
        val savedProduct = productRepository.save(Product(name = "상품1", stock = 10))
        val updateRequest = ProductUpdateRequestDto(name = "상품2", stock = 20)

        performAndDocument("상품 수정 - 실패, USER 권한으로 ADMIN API 접근 시도") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/products/{id}"
            urlVars = arrayOf(savedProduct.id)
            requestBody = updateRequest
            expectedStatus = status().isForbidden // 403 Forbidden 기대
            snippets = arrayOf(
                responseFields(commonErrorResponseSnippet())
            )
        }
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `재고 차감 - 실패, USER 권한으로 ADMIN API 접근 시도`() {
        val savedProduct = productRepository.save(Product(name = "테스트 상품", stock = 10))
        val quantity = 11

        performAndDocument("재고 차감 - 실패, USER 권한으로 ADMIN API 접근 시도") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/products/{id}/decrease-stock"
            urlVars = arrayOf(savedProduct.id)
            queryParams {
                add("quantity", quantity.toString())
            }
            expectedStatus = status().isForbidden // 403 Forbidden 기대
            snippets = arrayOf(
                responseFields(commonErrorResponseSnippet())
            )
        }
    }
}