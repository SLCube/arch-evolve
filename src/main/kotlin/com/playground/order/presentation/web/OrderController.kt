package com.playground.order.presentation.web

import com.playground.auth.domain.model.AuthUser
import com.playground.order.application.port.`in`.OrderUseCase
import com.playground.order.presentation.mapper.toCommand
import com.playground.order.presentation.request.OrderCreateRequestDto
import com.playground.order.presentation.response.OrderResponseDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(
    private val orderUseCase: OrderUseCase,
) {
    @PostMapping
    fun createOrder(
        @AuthenticationPrincipal authUser: AuthUser,
        @RequestBody @Valid request: OrderCreateRequestDto,
    ): ResponseEntity<OrderResponseDto> {
        val command = request.toCommand(authUser.userId)
        val createdOrder = orderUseCase.createOrder(command)

        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponseDto.toResponse(createdOrder))
    }
}
