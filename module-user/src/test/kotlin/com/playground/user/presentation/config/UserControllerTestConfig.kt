package com.playground.user.presentation.config

import com.playground.user.application.port.inbound.UserUseCase
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class UserControllerTestConfig {

    @Bean
    @Primary
    fun userUseCase(): UserUseCase {
        return Mockito.mock(UserUseCase::class.java)
    }
}