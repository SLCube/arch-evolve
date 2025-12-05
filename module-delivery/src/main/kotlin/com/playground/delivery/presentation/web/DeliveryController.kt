package com.playground.delivery.presentation.web

import com.playground.delivery.application.port.inbound.DeliveryUsecase
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/deliveries")
class DeliveryController(
    private val deliveryUsecase: DeliveryUsecase,
) {
}