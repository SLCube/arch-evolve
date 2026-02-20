package com.playground.payment.presentation.internal.annotation

import com.playground.payment.presentation.internal.config.PaymentInternalControllerTestConfig
import com.playground.payment.presentation.internal.controller.PaymentInternalController
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(PaymentInternalController::class)
@Import(PaymentInternalControllerTestConfig::class)
annotation class PaymentInternalControllerSliceTest
