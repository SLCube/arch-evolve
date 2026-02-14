package com.playground.user.presentation.config

import com.playground.user.application.port.inbound.UserAddressUseCase
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class UserAddressControllerTestConfig {

    @Bean
    @Primary
    fun userAddressUseCase(): UserAddressUseCase =
        Mockito.mock(UserAddressUseCase::class.java)
}