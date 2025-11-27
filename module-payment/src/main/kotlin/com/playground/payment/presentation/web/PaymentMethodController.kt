package com.playground.payment.presentation.web

import com.playground.payment.application.port.inbound.PaymentMethodUseCase
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payment-methods")
class PaymentMethodController(
    private val paymentMethodUseCase: PaymentMethodUseCase,
) {
}