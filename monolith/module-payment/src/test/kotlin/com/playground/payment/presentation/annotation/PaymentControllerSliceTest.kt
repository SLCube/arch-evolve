package com.playground.payment.presentation.annotation

import com.playground.payment.presentation.config.PaymentControllerTestConfig
import com.playground.payment.presentation.web.PaymentMethodController
import com.playground.support.security.config.TestSecurityConfig
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(PaymentMethodController::class)
@Import(PaymentControllerTestConfig::class, TestSecurityConfig::class)
annotation class PaymentControllerSliceTest
