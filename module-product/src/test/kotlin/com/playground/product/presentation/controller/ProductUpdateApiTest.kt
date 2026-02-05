package com.playground.product.presentation.controller

import com.playground.product.application.port.inbound.ProductUseCase
import com.playground.product.fixture.application.domain.ProductDomainTestFixture
import com.playground.product.fixture.presentation.request.ProductRequestTestFIxture
import com.playground.product.presentation.annotation.ProductControllerSliceTest
import com.playground.product.presentation.mapper.toCommand
import com.playground.support.RestDocsTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import com.playground.support.security.annotation.WithMockAuthUser
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@ProductControllerSliceTest
class ProductUpdateApiTest(
    @param:Autowired private val productUseCase: ProductUseCase,
): RestDocsTest() {

    @Test
    @WithMockAuthUser(role = "ADMIN")
    fun `상품 수정 - 성공`() {
        // given
        val mockProductUpdateRequest = ProductRequestTestFIxture.mockProductUpdateRequest()
        val mockUpdatedProduct = ProductDomainTestFixture.mockProduct(
            name = mockProductUpdateRequest.name,
            stock = mockProductUpdateRequest.stock,
            price = mockProductUpdateRequest.price,
        )
        val mockCommand = mockProductUpdateRequest.toCommand(mockUpdatedProduct.id!!)

        given(productUseCase.updateProduct(eq(mockCommand)))
            .willReturn(mockUpdatedProduct)

        // when & then
        performAndDocument("상품 수정 - 성공") {
            tag = "상품 API"
            summary = "상품 수정"
            description = "기존 상품의 정보를 수정합니다. ADMIN 권한이 필요합니다."

            httpMethod = HttpMethod.PATCH
            urlTemplate = "/products/{id}"
            urlVars = arrayOf(mockUpdatedProduct.id)
            requestBody = mockProductUpdateRequest
            expectedStatus = status().isOk
            additionalMatchers =
                arrayOf(
                    jsonPath("$.name").value(mockUpdatedProduct.name),
                    jsonPath("$.stock").value(mockUpdatedProduct.stock),
                    jsonPath("$.price").value(mockUpdatedProduct.price),
                )
            snippets =
                arrayOf(
                    requestFields(
                        fieldWithPath("name").description("상품 이름"),
                        fieldWithPath("stock").description("재고량"),
                        fieldWithPath("price").description("상품 가격"),
                    ),
                    responseFields(
                        fieldWithPath("id").description("상품 ID"),
                        fieldWithPath("name").description("상품 이름"),
                        fieldWithPath("stock").description("재고량"),
                        fieldWithPath("price").description("상품 가격"),
                    )
                )
        }
    }

    @Test
    @WithMockAuthUser(role = "ADMIN")
    fun `상품 수정 - 실패, 이름이 비어있음`() {
        // given
        val mockProductUpdateRequest = ProductRequestTestFIxture.mockProductUpdateRequest(
            name = "",
        )
        val mockUpdatedProduct = ProductDomainTestFixture.mockProduct(
            name = mockProductUpdateRequest.name,
            stock = mockProductUpdateRequest.stock,
            price = mockProductUpdateRequest.price,
        )
        val mockCommand = mockProductUpdateRequest.toCommand(mockUpdatedProduct.id!!)

        given(productUseCase.updateProduct(eq(mockCommand)))
            .willReturn(mockUpdatedProduct)

        // when & then
        performAndDocument("상품 수정 - 실패, 이름이 비어있음") {
            tag = "상품 API"
            summary = "상품 수정 실패 - 유효성 검증"
            description = "상품 이름이 비어있을 경우 400 Bad Request를 반환합니다."

            httpMethod = HttpMethod.PATCH
            urlTemplate = "/products/{id}"
            urlVars = arrayOf(mockUpdatedProduct.id)
            requestBody = mockProductUpdateRequest
            expectedStatus = status().isBadRequest
            additionalMatchers =
                arrayOf(
                    jsonPath("$.message").value("입력값이 유효하지 않습니다."),
                    jsonPath("$.errors.name").value("상품 이름은 필수입니다.")
                )
            snippets =
                arrayOf(
                    responseFields(
                        commonErrorResponseSnippet() +
                                fieldWithPath("errors.name").description("상품 이름 필드의 에러 메세지")
                    )
                )
        }
    }

    @Test
    @WithMockAuthUser(role = "USER")
    fun `상품 수정 - 실패, USER 권한으로 ADMIN API 접근 시도`() {
        // given
        val mockProductUpdateRequest = ProductRequestTestFIxture.mockProductUpdateRequest()
        val mockUpdatedProduct = ProductDomainTestFixture.mockProduct(
            name = mockProductUpdateRequest.name,
            stock = mockProductUpdateRequest.stock,
            price = mockProductUpdateRequest.price,
        )

        val productId = mockUpdatedProduct.id!!

        performAndDocument("상품 수정 - 실패, USER 권한으로 ADMIN API 접근 시도") {
            tag = "상품 API"
            summary = "상품 수정 실패 - 권한 부족"
            description = "USER 권한으로 ADMIN 전용 API에 접근할 경우 403 Forbidden을 반환합니다."

            httpMethod = HttpMethod.PATCH
            urlTemplate = "/products/{id}"
            urlVars = arrayOf(productId)
            requestBody = mockProductUpdateRequest
            expectedStatus = status().isForbidden
            snippets =
                arrayOf(
                    responseFields(commonErrorResponseSnippet()),
                )
        }

        verify(productUseCase, never()).updateProduct(any())
    }
}