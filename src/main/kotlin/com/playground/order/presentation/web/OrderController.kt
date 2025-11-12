package com.playground.order.presentation.web

import com.playground.order.presentation.request.OrderCreateRequestDto
import com.playground.order.presentation.response.OrderResponseDto
import com.playground.order.application.service.OrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderService: OrderService
) {

    @PostMapping
    fun createOrder(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestBody @Valid request: OrderCreateRequestDto
    ): ResponseEntity<OrderResponseDto> {
        val loginId = userDetails.username
        val createdOrder = orderService.createOrder(loginId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder)
    }
}