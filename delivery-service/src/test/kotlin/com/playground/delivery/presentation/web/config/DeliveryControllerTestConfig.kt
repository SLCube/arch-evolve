package com.playground.delivery.presentation.web.config

import com.playground.delivery.application.port.inbound.DeliveryQueryUseCase
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class DeliveryControllerTestConfig {
    @Bean
    @Primary
    fun deliveryQueryUseCase(): DeliveryQueryUseCase = Mockito.mock(DeliveryQueryUseCase::class.java)
}
