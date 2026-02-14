package com.playground.payment.presentation.config

import com.playground.payment.application.port.inbound.PaymentMethodUseCase
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class PaymentControllerTestConfig {

    @Bean
    @Primary
    fun paymentMethodUseCase(): PaymentMethodUseCase = Mockito.mock(PaymentMethodUseCase::class.java)
}
