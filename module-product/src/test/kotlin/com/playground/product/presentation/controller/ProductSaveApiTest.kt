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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
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
class ProductSaveApiTest(
    @param:Autowired private val productUseCase: ProductUseCase,
) : RestDocsTest() {

    @BeforeEach
    fun setUp() {
        reset(productUseCase)
    }

    @Test
    @WithMockAuthUser(role = "ADMIN")
    fun `상품 등록 - 성공`() {
        // given
        val request = ProductRequestTestFIxture.mockProductSaveRequest()
        val mockSavedProduct = ProductDomainTestFixture.mockProduct()

        given(productUseCase.saveProduct(eq(request.toCommand())))
            .willReturn(mockSavedProduct)

        // when & then
        performAndDocument("상품 등록 - 성공") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/products"
            requestBody = request
            expectedStatus = status().isCreated
            additionalMatchers = arrayOf(
                jsonPath("$.id").value(mockSavedProduct.id),
                jsonPath("$.name").value(mockSavedProduct.name),
                jsonPath("$.stock").value(mockSavedProduct.stock),
                jsonPath("$.price").value(mockSavedProduct.price),
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
    fun `상품 등록 - 실패, 이름이 비어있음`() {
        // given
        val request = ProductRequestTestFIxture.mockProductSaveRequest(
            name = "",
        )

        // when & then
        performAndDocument("상품 등록 - 실패, 이름이 비어있음") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/products"
            requestBody = request
            expectedStatus = status().isBadRequest
            additionalMatchers = arrayOf(
                jsonPath("$.message").value("입력값이 유효하지 않습니다."),
                jsonPath("$.errors.name").value("상품 이름은 필수입니다."),
            )
            snippets =
                arrayOf(
                    responseFields(
                        commonErrorResponseSnippet() +
                                fieldWithPath("errors.name").description("상품 이름 필드의 에러 메세지")
                    ),
                )
        }

        verify(productUseCase, never()).saveProduct(any())
    }

    @Test
    @WithMockAuthUser(role = "USER")
    fun `상품 등록 - 실패, USER 권한으로 ADMIN API 접근 시도`() {
        // given
        val request = ProductRequestTestFIxture.mockProductSaveRequest()

        // when & then
        performAndDocument("상품 등록 - 실패, USER 권한으로 ADMIN API 접근 시도") {
            httpMethod = HttpMethod.POST
            urlTemplate = "/products"
            requestBody = request
            expectedStatus = status().isForbidden
            snippets = arrayOf(
                responseFields(
                    commonErrorResponseSnippet()
                )
            )
        }

        verify(productUseCase, never()).saveProduct(any())
    }
}