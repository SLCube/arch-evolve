package com.playground.user.presentation.annotation

import com.playground.support.security.config.TestSecurityConfig
import com.playground.user.presentation.config.UserControllerTestConfig
import com.playground.user.presentation.web.UserController
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(UserController::class)
@Import(UserControllerTestConfig::class, TestSecurityConfig::class)
annotation class UserControllerSliceTest
