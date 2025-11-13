package com.playground.product.controller

import com.playground.product.persistence.entity.ProductJpaEntity
import com.playground.product.persistence.repository.ProductRepository
import com.playground.product.presentation.request.ProductUpdateRequestDto
import com.playground.support.ApiTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.*
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@WithMockUser
class ProductUpdateApiTest(
    @param:Autowired private val productRepository: ProductRepository
) : ApiTest() {

    @Test
    @WithMockUser(roles = ["ADMIN"])
    fun `상품 수정 - 성공`() {
        val savedProductJpaEntity = productRepository.save(ProductJpaEntity(name = "상품1", stock = 10, price = 10000L))
        val updateRequest = ProductUpdateRequestDto(name = "상품2", stock = 20, price = 20000L)

        performAndDocument("상품 수정 - 성공") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/products/{id}"
            urlVars = arrayOf(savedProductJpaEntity.id)
            requestBody = updateRequest
            expectedStatus = status().isOk
            additionalMatchers = arrayOf(
                jsonPath("$.name").value("상품2"),
                jsonPath("$.stock").value(20),
                jsonPath("$.price").value(20000L)
            )
            snippets = arrayOf(
                requestFields(
                    fieldWithPath("name").description("상품 이름"),
                    fieldWithPath("stock").description("재고량"),
                    fieldWithPath("price").description("상품 가격")
                ),
                responseFields(
                    fieldWithPath("id").description("상품 ID"),
                    fieldWithPath("name").description("상품 이름"),
                    fieldWithPath("stock").description("재고량"),
                    fieldWithPath("price").description("상품 가격")
                )
            )
        }
    }

    @Test
    @WithMockUser(roles = ["USER"])
    fun `상품 수정 - 실패, USER 권한으로 ADMIN API 접근 시도`() {
        val savedProductJpaEntity = productRepository.save(ProductJpaEntity(name = "상품1", stock = 10, price = 10000L))
        val updateRequest = ProductUpdateRequestDto(name = "상품2", stock = 20, price = 20000L)

        performAndDocument("상품 수정 - 실패, USER 권한으로 ADMIN API 접근 시도") {
            httpMethod = HttpMethod.PATCH
            urlTemplate = "/products/{id}"
            urlVars = arrayOf(savedProductJpaEntity.id)
            requestBody = updateRequest
            expectedStatus = status().isForbidden // 403 Forbidden 기대
            snippets = arrayOf(
                responseFields(commonErrorResponseSnippet())
            )
        }
    }
}