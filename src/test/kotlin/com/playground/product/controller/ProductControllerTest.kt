package com.playground.product.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.product.controller.request.ProductSaveRequestDto
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@Suppress("NonAsciiCharacters")
@AutoConfigureMockMvc
@SpringBootTest
class ProductControllerTest(
    @param:Autowired private val mockMvc: MockMvc,
    @param:Autowired private val objectMapper: ObjectMapper,
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
}