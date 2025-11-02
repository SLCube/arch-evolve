package com.playground.product.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@Suppress("NonAsciiCharacters")
@SpringBootTest
class ProductServiceTest(
    @param:Autowired private val productService: ProductService
) {

    @Test
    fun `상품을 저장한다`() {
        // given
        val name = "상품1"
        val stock = 10

        // when
        val savedProduct = productService.save(name, stock)

        // then
        assertThat(savedProduct.id).isNotNull()
        assertThat(savedProduct.name).isEqualTo(name)
        assertThat(savedProduct.stock).isEqualTo(stock)
    }
}