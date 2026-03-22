package com.playground.delivery.presentation.internal.annotation

import com.playground.delivery.presentation.internal.config.DeliveryInternalControllerTestConfig
import com.playground.delivery.presentation.internal.controller.DeliveryInternalController
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(DeliveryInternalController::class)
@Import(DeliveryInternalControllerTestConfig::class)
annotation class DeliveryInternalControllerSliceTest
