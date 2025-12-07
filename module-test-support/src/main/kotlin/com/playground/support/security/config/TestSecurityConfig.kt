package com.playground.support.security.config

import com.playground.common.constant.ApiConstants
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@TestConfiguration
@EnableWebSecurity
class TestSecurityConfig {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        return http.csrf { it.disable() }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/users/sign-up", "/users/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, ApiConstants.PRODUCT_API_BASE_PATH)
                    .hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, ApiConstants.PRODUCT_API_BASE_PATH)
                    .hasAnyRole("ADMIN")
                    .requestMatchers(HttpMethod.PATCH, ApiConstants.PRODUCT_API_BASE_PATH)
                    .hasAnyRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, ApiConstants.PRODUCT_API_BASE_PATH)
                    .hasAnyRole("ADMIN")
                    .anyRequest()
                    .authenticated()
            }
            .build()
    }
}