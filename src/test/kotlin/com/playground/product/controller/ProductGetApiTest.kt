package com.playground.product.controller

import com.playground.common.error.ErrorCode
import com.playground.product.persistence.entity.ProductJpaEntity
import com.playground.product.persistence.repository.ProductRepository
import com.playground.support.ApiTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.AfterEach
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
class ProductGetApiTest(
    @param:Autowired private val productRepository: ProductRepository
) : ApiTest() {

    @AfterEach
    fun cleanUpProduct() {
        productRepository.deleteAll()
    }

    @Test
    fun `상품 단일 조회 - 성공`() {
        val savedProductJpaEntity = productRepository.save(ProductJpaEntity(name = "상품1", stock = 10, price = 10000L))

        performAndDocument("상품 단일 조회 - 성공") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/products/{id}"
            urlVars = arrayOf(savedProductJpaEntity.id)
            expectedStatus = status().isOk
            additionalMatchers = arrayOf(
                jsonPath("$.id").value(savedProductJpaEntity.id),
                jsonPath("$.name").value(savedProductJpaEntity.name),
                jsonPath("$.stock").value(savedProductJpaEntity.stock),
                jsonPath("$.price").value(savedProductJpaEntity.price)
            )
            snippets = arrayOf(
                pathParameters(
                    parameterWithName("id").description("상품 ID")
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
        productRepository.save(ProductJpaEntity(name = "상품1", stock = 10, price = 10000L))
        productRepository.save(ProductJpaEntity(name = "상품2", stock = 20, price = 20000L))

        performAndDocument("상품 목록 조회 - 성공") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/products"
            expectedStatus = status().isOk
            additionalMatchers = arrayOf(
                jsonPath("$.length()").value(2)
            )
        }
    }
}