package com.playground.delivery.presentation.internal.controller

import com.playground.delivery.application.port.inbound.DeliveryQueryUseCase
import com.playground.delivery.presentation.internal.response.DeliveryInfoResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/deliveries")
class DeliveryInternalController(
    private val deliveryQueryUseCase: DeliveryQueryUseCase,
) {
    @GetMapping("/{orderId}")
    fun getDeliveryByOrderId(
        @PathVariable orderId: Long,
    ): ResponseEntity<DeliveryInfoResponse> {
        val delivery = deliveryQueryUseCase.getDeliveryByOrderId(orderId)
        return ResponseEntity.ok(DeliveryInfoResponse.from(delivery))
    }
}
