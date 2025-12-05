package com.playground.order.presentation.config

import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.OrderQueryUseCase
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class OrderControllerTestConfig {

    @Bean
    @Primary
    fun orderCommandUsecase(): OrderCommandUseCase {
        return Mockito.mock(OrderCommandUseCase::class.java)
    }

    @Bean
    @Primary
    fun orderQueryUsecase(): OrderQueryUseCase {
        return Mockito.mock(OrderQueryUseCase::class.java)
    }
}