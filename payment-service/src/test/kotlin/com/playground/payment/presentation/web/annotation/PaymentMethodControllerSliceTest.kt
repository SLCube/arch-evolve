package com.playground.payment.presentation.web.annotation

import com.playground.payment.presentation.web.config.PaymentMethodControllerTestConfig
import com.playground.payment.presentation.web.controller.PaymentMethodController
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(PaymentMethodController::class)
@Import(PaymentMethodControllerTestConfig::class)
annotation class PaymentMethodControllerSliceTest
