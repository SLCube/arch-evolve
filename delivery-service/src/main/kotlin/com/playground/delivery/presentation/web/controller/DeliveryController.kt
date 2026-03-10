package com.playground.delivery.presentation.web.controller

import com.playground.delivery.application.port.inbound.DeliveryQueryUseCase
import com.playground.delivery.domain.exception.ForbiddenException
import com.playground.delivery.presentation.support.AuthUserId
import com.playground.delivery.presentation.web.response.DeliveryResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/deliveries")
class DeliveryController(
    private val deliveryQueryUseCase: DeliveryQueryUseCase,
) {
    @GetMapping("/orders/{orderId}")
    fun getDeliveryByOrderId(
        @AuthUserId userId: Long,
        @PathVariable orderId: Long,
    ): ResponseEntity<DeliveryResponse> {
        val delivery = deliveryQueryUseCase.getDeliveryByOrderId(orderId)
        if (delivery.userId != userId) throw ForbiddenException()
        return ResponseEntity.ok(DeliveryResponse.from(delivery))
    }
}
