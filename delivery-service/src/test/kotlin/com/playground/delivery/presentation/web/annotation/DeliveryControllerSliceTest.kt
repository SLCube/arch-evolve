package com.playground.delivery.presentation.web.annotation

import com.playground.delivery.presentation.web.config.DeliveryControllerTestConfig
import com.playground.delivery.presentation.web.controller.DeliveryController
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(DeliveryController::class)
@Import(DeliveryControllerTestConfig::class)
annotation class DeliveryControllerSliceTest
