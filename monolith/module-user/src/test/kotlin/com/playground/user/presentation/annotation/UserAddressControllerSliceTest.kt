package com.playground.user.presentation.annotation

import com.playground.support.security.config.TestSecurityConfig
import com.playground.user.presentation.config.UserAddressControllerTestConfig
import com.playground.user.presentation.web.UserAddressController
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(UserAddressController::class)
@Import(UserAddressControllerTestConfig::class, TestSecurityConfig::class)
annotation class UserAddressControllerSliceTest
