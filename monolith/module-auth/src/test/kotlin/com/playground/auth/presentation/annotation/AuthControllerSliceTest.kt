package com.playground.auth.presentation.annotation

import com.playground.auth.presentation.config.AuthenticationMockConfig
import com.playground.auth.presentation.config.AuthTestSecurityConfig
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest
@Import(AuthTestSecurityConfig::class, AuthenticationMockConfig::class)
annotation class AuthControllerSliceTest
