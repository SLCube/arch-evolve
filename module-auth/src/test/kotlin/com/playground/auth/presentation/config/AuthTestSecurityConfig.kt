package com.playground.auth.presentation.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.auth.application.port.inbound.TokenIssueUseCase
import com.playground.auth.filter.JsonAuthenticationFilter
import com.playground.auth.handler.LoginFailureHandler
import com.playground.auth.handler.LoginSuccessHandler
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@TestConfiguration
class AuthTestSecurityConfig {

    @Bean
    fun loginSuccessHandler(
        tokenIssueUseCase: TokenIssueUseCase,
        objectMapper: ObjectMapper,
    ): LoginSuccessHandler = LoginSuccessHandler(tokenIssueUseCase, objectMapper)

    @Bean
    fun loginFailureHandler(objectMapper: ObjectMapper): LoginFailureHandler = LoginFailureHandler(objectMapper)

    @Bean
    fun authSecurityFilterChain(
        http: HttpSecurity,
        authenticationManager: AuthenticationManager,
        objectMapper: ObjectMapper,
        loginSuccessHandler: LoginSuccessHandler,
        loginFailureHandler: LoginFailureHandler,
    ): SecurityFilterChain {
        val jsonAuthenticationFilter = JsonAuthenticationFilter(objectMapper, authenticationManager).apply {
            setAuthenticationSuccessHandler(loginSuccessHandler)
            setAuthenticationFailureHandler(loginFailureHandler)
        }

        return http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { it.anyRequest().permitAll() }
            .addFilterAt(jsonAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .build()
    }
}
