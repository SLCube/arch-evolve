package com.playground.order.presentation.annotation

import com.playground.order.presentation.config.OrderControllerTestConfig
import com.playground.order.presentation.web.OrderController
import com.playground.support.security.config.TestSecurityConfig
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(OrderController::class)
@Import(OrderControllerTestConfig::class, TestSecurityConfig::class)
annotation class OrderControllerSliceTest
