package com.playground.product.presentation.controller

import com.playground.common.error.ErrorCode
import com.playground.product.application.port.inbound.ProductUseCase
import com.playground.product.domain.exception.ProductNotFoundException
import com.playground.product.fixture.application.domain.ProductDomainTestFixture
import com.playground.product.fixture.application.query.ProductQueryTestFixture
import com.playground.product.presentation.annotation.ProductControllerSliceTest
import com.playground.support.RestDocsTest
import com.playground.support.docs.ApiDocumentUtils.commonErrorResponseSnippet
import com.playground.support.docs.performAndDocument
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpMethod
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@Suppress("NonAsciiCharacters")
@ProductControllerSliceTest
class ProductLookUpApiTest(
    @param:Autowired private val productUseCase: ProductUseCase,
): RestDocsTest() {

    @Test
    fun `상품 단일 조회 - 성공`() {
        // given
        val mockProduct = ProductDomainTestFixture.mockProduct()
        val mockProductQuery = ProductQueryTestFixture.mockProductQuery(id = mockProduct.id!!)

        given(productUseCase.getProduct(eq(mockProductQuery)))
            .willReturn(mockProduct)

        // when & then
        performAndDocument("상품 단일 조회 - 성공") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/products/{id}"
            urlVars = arrayOf(mockProduct.id)
            expectedStatus = status().isOk
            additionalMatchers = arrayOf(
                jsonPath("$.id").value(mockProduct.id),
                jsonPath("$.name").value(mockProduct.name),
                jsonPath("$.stock").value(mockProduct.stock),
                jsonPath("$.price").value(mockProduct.price),
            )
            snippets =
                arrayOf(
                    pathParameters(
                        parameterWithName("id").description("상품 ID")
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
    fun `상품 단일 조회 - 실패, 존재하지 않는 상품`() {
        // given
        val nonExistingProductId = 999L

        val mockProductQuery = ProductQueryTestFixture.mockProductQuery(id = nonExistingProductId)
        given(productUseCase.getProduct(eq(mockProductQuery)))
            .willThrow(ProductNotFoundException(nonExistingProductId))


        // when & then
        performAndDocument("상품 단일 조회 - 실패, 존재하지 않는 상품") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/products/{id}"
            urlVars = arrayOf(nonExistingProductId)
            expectedStatus = status().isNotFound
            additionalMatchers =
                arrayOf(
                    jsonPath("$.code").value(ErrorCode.PRODUCT_NOT_FOUND.code),
                    jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.message(nonExistingProductId)),
                )
            snippets =
                arrayOf(
                    responseFields(commonErrorResponseSnippet())
                )
        }
    }

    @Test
    fun `상품 목록 조회 - 성공`() {
        // given
        val mockProducts = ProductDomainTestFixture.mockProducts(count = 2)

        given(productUseCase.getAllProducts())
            .willReturn(mockProducts)

        // when & then
        performAndDocument("상품 목록 조회 - 성공") {
            httpMethod = HttpMethod.GET
            urlTemplate = "/products"
            expectedStatus = status().isOk
            additionalMatchers = arrayOf(
                jsonPath("$.length()").value(mockProducts.size),
                jsonPath("$.[0].id").value(mockProducts[0].id),
                jsonPath("$.[0].name").value(mockProducts[0].name),
                jsonPath("$.[0].stock").value(mockProducts[0].stock),
                jsonPath("$.[0].price").value(mockProducts[0].price),
                jsonPath("$.[1].id").value(mockProducts[1].id),
                jsonPath("$.[1].name").value(mockProducts[1].name),
                jsonPath("$.[1].stock").value(mockProducts[1].stock),
                jsonPath("$.[1].price").value(mockProducts[1].price),
            )
            snippets =
                arrayOf(
                    responseFields(
                        fieldWithPath("[].id").description("상품 ID"),
                        fieldWithPath("[].name").description("상품 이름"),
                        fieldWithPath("[].stock").description("재고량"),
                        fieldWithPath("[].price").description("상품 가격"),
                    )
                )
        }
    }
}