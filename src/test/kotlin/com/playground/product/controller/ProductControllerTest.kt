package com.playground.product.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.product.controller.request.ProductSaveRequestDto
import com.playground.product.controller.request.ProductUpdateRequestDto
import com.playground.product.repository.ProductRepository
import com.playground.product.service.ProductService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put

@Suppress("NonAsciiCharacters")
@AutoConfigureMockMvc
@SpringBootTest
class ProductControllerTest(
    @param:Autowired private val mockMvc: MockMvc,
    @param:Autowired private val objectMapper: ObjectMapper,
    @param:Autowired private val productService: ProductService,
    @param:Autowired private val productRepository: ProductRepository,
) {

    @AfterEach
    fun tearDown() {
        productRepository.deleteAll()
    }

    @Test
    fun `상품을 저장한다`() {
        val productSaveRequestDto = ProductSaveRequestDto(
            name = "상품1",
            stock = 10
        )

        mockMvc.post("/products") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(productSaveRequestDto)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.name") { value("상품1") }
            jsonPath("$.stock") { value(10) }
        }
    }

    @Test
    fun `상품을 조회한다`() {
        val name = "상품1"
        val stock = 10
        val savedProduct = productService.save(name, stock)

        mockMvc.get("/products/{id}", savedProduct.id)
            .andExpect {
                status { isOk() }
                jsonPath("$.id") { value(savedProduct.id) }
                jsonPath("$.name") { value(name) }
                jsonPath("$.stock") { value(stock) }
            }
    }

    @Test
    fun `상품 조회를 실패한다`() {
        val nonExistingId = 999L
        mockMvc.get("/products/{id}", nonExistingId)
            .andExpect {
                status { isNotFound() }
                jsonPath("$.message") { value("id: $nonExistingId, 상품을 찾을 수 없습니다.") }
            }
    }

    @Test
    fun `상품 목록을 조회한다`() {
        productService.save("상품1", 10)
        productService.save("상품2", 20)

        mockMvc.get("/products")
            .andExpect {
                status { isOk() }
                jsonPath("$") { isArray() }
                jsonPath("$.length()") { value(2) }
                jsonPath("$[0].name") { value("상품1") }
                jsonPath("$[0].stock") { value(10) }
                jsonPath("$[1].name") { value("상품2") }
                jsonPath("$[1].stock") { value(20) }
            }
    }

    @Test
    fun `상품 정보를 수정한다`() {
        val name = "상품1"
        val stock = 10
        val savedProduct = productService.save(name, stock)

        val updatedName = "상품2"
        val updatedStock = 20
        val productUpdateRequestDto = ProductUpdateRequestDto(
            name = updatedName,
            stock = updatedStock
        )

        mockMvc.put("/products/{id}", savedProduct.id) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(productUpdateRequestDto)
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value(updatedName) }
            jsonPath("$.stock") { value(updatedStock) }
        }
    }
}