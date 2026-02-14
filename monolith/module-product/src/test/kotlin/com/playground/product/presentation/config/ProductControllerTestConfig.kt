package com.playground.product.presentation.config

import com.playground.product.application.port.inbound.ProductUseCase
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class ProductControllerTestConfig {

    @Bean
    @Primary
    fun productUseCase(): ProductUseCase {
        return Mockito.mock(ProductUseCase::class.java)
    }

}