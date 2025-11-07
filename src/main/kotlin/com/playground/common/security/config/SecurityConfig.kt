package com.playground.common.security.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.playground.common.constant.ApiConstants
import com.playground.common.security.filter.JsonAuthenticationFilter
import com.playground.common.security.handler.CustomAccessDeniedHandler
import com.playground.common.security.handler.CustomAuthenticationEntryPoint
import com.playground.common.security.handler.LoginFailureHandler
import com.playground.common.security.handler.LoginSuccessHandler
import com.playground.common.security.jwt.JwtAuthenticationFilter
import com.playground.common.security.jwt.JwtTokenProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val loginSuccessHandler: LoginSuccessHandler,
    private val loginFailureHandler: LoginFailureHandler,
    private val accessDeniedHandler: CustomAccessDeniedHandler,
    private val authenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val objectMapper: ObjectMapper,
    private val authenticationConfiguration: AuthenticationConfiguration
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        val authenticationManager = authenticationConfiguration.authenticationManager

        val jsonAuthenticationFilter = JsonAuthenticationFilter(objectMapper, authenticationManager)
        jsonAuthenticationFilter.setAuthenticationSuccessHandler(loginSuccessHandler)
        jsonAuthenticationFilter.setAuthenticationFailureHandler(loginFailureHandler)

        return http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/users/sign-up", "/users/login").permitAll()
                    .requestMatchers(HttpMethod.GET, ApiConstants.PRODUCT_API_BASE_PATH).hasAnyRole("USER", "ADMIN")
                    .requestMatchers(HttpMethod.POST, ApiConstants.PRODUCT_API_BASE_PATH).hasAnyRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, ApiConstants.PRODUCT_API_BASE_PATH).hasAnyRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(
                JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter::class.java
            )
            .addFilterAt(
                jsonAuthenticationFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
            .exceptionHandling {
                it.authenticationEntryPoint(authenticationEntryPoint)
                it.accessDeniedHandler(accessDeniedHandler)
            }
            .build()
    }
}