package com.playground.order.presentation.controller

import com.playground.order.application.port.inbound.OrderCommandUseCase
import com.playground.order.application.port.inbound.OrderQueryUseCase
import com.playground.order.presentation.config.OrderControllerTestConfig
import com.playground.order.presentation.web.OrderController
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(OrderController::class)
@Import(OrderControllerTestConfig::class)
@Suppress("NonAsciiCharacters")
class OrderCreateApiTest {

    @Autowired
    private lateinit var orderCommandUseCase: OrderCommandUseCase

    @Autowired
    private lateinit var orderQueryUseCase: OrderQueryUseCase

    @Test
    fun test() {

    }
}