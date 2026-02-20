package com.playground.payment.presentation.internal.config

import com.playground.payment.application.port.inbound.PaymentUseCase
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class PaymentInternalControllerTestConfig {
    @Bean
    @Primary
    fun paymentUseCase(): PaymentUseCase = Mockito.mock(PaymentUseCase::class.java)
}
