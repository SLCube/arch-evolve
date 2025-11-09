package com.playground.product.controller

import com.playground.product.controller.request.ProductSaveRequestDto
import com.playground.product.repository.ProductRepository
import com.playground.support.ApiTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@WithMockUser
class ProductSaveApiTest(
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
}