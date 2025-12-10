package com.playground.auth.presentation.config

import com.playground.auth.jwt.JwtTokenProvider
import org.mockito.kotlin.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.AuthenticationManager

@TestConfiguration
class AuthenticationMockConfig {

    @Bean
    @Primary
    fun authenticationManager(): AuthenticationManager = mock()

    @Bean
    @Primary
    fun jwtTokenProvider(): JwtTokenProvider = mock()
}
