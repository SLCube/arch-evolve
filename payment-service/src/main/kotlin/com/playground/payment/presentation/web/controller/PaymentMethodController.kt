package com.playground.payment.presentation.web.controller

import com.playground.payment.application.port.inbound.PaymentMethodUseCase
import com.playground.payment.application.port.inbound.command.PaymentMethodDeleteCommand
import com.playground.payment.presentation.web.mapper.toCommand
import com.playground.payment.presentation.web.request.PaymentMethodRegisterRequestDto
import com.playground.payment.presentation.web.response.PaymentMethodResponseDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payment-methods")
class PaymentMethodController(
    private val paymentMethodUseCase: PaymentMethodUseCase,
) {
    @PostMapping
    fun registerPaymentMethod(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestBody @Valid request: PaymentMethodRegisterRequestDto,
    ): ResponseEntity<PaymentMethodResponseDto> {
        val createdPaymentMethod = paymentMethodUseCase.registerPaymentMethod(request.toCommand(userId))
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentMethodResponseDto.toResponse(createdPaymentMethod))
    }

    @GetMapping
    fun getPaymentMethodList(
        @RequestHeader("X-User-Id") userId: Long,
    ): ResponseEntity<List<PaymentMethodResponseDto>> {
        val response = paymentMethodUseCase.getPaymentMethodList(userId).map { PaymentMethodResponseDto.toResponse(it) }
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{paymentMethodId}")
    fun deletePaymentMethod(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable paymentMethodId: Long,
    ): ResponseEntity<Unit> {
        paymentMethodUseCase.deletePaymentMethod(
            PaymentMethodDeleteCommand(
                userId = userId,
                paymentMethodId = paymentMethodId,
            ),
        )
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}
