package com.playground.product.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.common.error.ErrorCode
import com.playground.product.controller.request.ProductSaveRequestDto
import com.playground.product.controller.request.ProductUpdateRequestDto
import com.playground.product.domain.Product
import com.playground.product.repository.ProductRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@Suppress("NonAsciiCharacters")
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser
class ProductControllerTest(
    @param:Autowired private val mockMvc: MockMvc,
    @param:Autowired private val objectMapper: ObjectMapper,
    @param:Autowired private val productRepository: ProductRepository
) {

    @AfterEach
    fun tearDown() {
        productRepository.deleteAll()
    }

    @Test
    fun `상품 등록 - 성공`() {
        val request = ProductSaveRequestDto(
            name = "상품1",
            stock = 10
        )

        mockMvc.post("/products") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.name") { value("상품1") }
            jsonPath("$.stock") { value(10) }
        }
    }

    @Test
    fun `상품 등록 - 실패, 이름이 비어있음`() {
        val request = ProductSaveRequestDto(
            name = "",
            stock = 10
        )

        mockMvc.post("/products") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors.name") { value("상품 이름은 필수입니다.") }
        }
    }

    @Test
    fun `상품 등록 - 실패, 재고가 0보다 작음`() {
        val request = ProductSaveRequestDto(
            name = "상품1",
            stock = -1
        )

        mockMvc.post("/products") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors.stock") { value("재고량은 0보다 커야 합니다.") }
        }
    }

    @Test
    fun `상품 단일 조회 - 성공`() {
        val savedProduct = productRepository.save(Product(name = "상품1", stock = 10))

        mockMvc.get("/products/{id}", savedProduct.id)
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(savedProduct.id) }
                jsonPath("$.name") { value(savedProduct.name) }
                jsonPath("$.stock") { value(savedProduct.stock) }
            }
    }

    @Test
    fun `상품 단일 조회 - 실패, 존재하지 않는 상품`() {
        val nonExistingId = 999L

        mockMvc.get("/products/{id}", nonExistingId)
            .andExpect {
                status { isNotFound() }
                jsonPath("$.code") { value(ErrorCode.PRODUCT_NOT_FOUND.code) }
                jsonPath("$.message") { value(ErrorCode.PRODUCT_NOT_FOUND.message(nonExistingId)) }
            }
    }

    @Test
    fun `상품 목록 조회 - 성공`() {
        productRepository.save(Product(name = "상품1", stock = 10))
        productRepository.save(Product(name = "상품2", stock = 20))

        mockMvc.get("/products")
            .andExpect {
                status { isOk() }
                jsonPath("$") { isArray() }
                jsonPath("$.length()") { value(2) }
            }
    }

    @Test
    fun `상품 수정 - 성공`() {
        val savedProduct = productRepository.save(Product(name = "상품1", stock = 10))
        val updateRequest = ProductUpdateRequestDto(name = "상품2", stock = 20)

        mockMvc.put("/products/{id}", savedProduct.id) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(updateRequest)
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("상품2") }
            jsonPath("$.stock") { value(20) }
        }
    }

    @Test
    fun `재고 차감 - 실패, 재고 부족`() {
        val savedProduct = productRepository.save(Product(name = "테스트 상품", stock = 10))
        val quantity = 11

        mockMvc.post("/products/{id}/decrease-stock", savedProduct.id) {
            param("quantity", quantity.toString())
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value(ErrorCode.INSUFFICIENT_STOCK.code) }
            jsonPath("$.message") { value(ErrorCode.INSUFFICIENT_STOCK.message(savedProduct.id, savedProduct.stock, quantity)) }
        }
    }
}