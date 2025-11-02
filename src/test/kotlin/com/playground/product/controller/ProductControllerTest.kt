package com.playground.product.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.product.controller.request.ProductSaveRequestDto
import com.playground.product.service.ProductService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@Suppress("NonAsciiCharacters")
@AutoConfigureMockMvc
@SpringBootTest
class ProductControllerTest(
    @param:Autowired private val mockMvc: MockMvc,
    @param:Autowired private val objectMapper: ObjectMapper,
    @param:Autowired private val productService: ProductService,
) {

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
}