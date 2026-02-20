package com.playground.payment.presentation.internal.controller

import com.playground.payment.application.port.inbound.PaymentUseCase
import com.playground.payment.presentation.internal.request.PaymentAuthorizeRequestDto
import com.playground.payment.presentation.internal.response.PaymentAuthorizeResponseDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/internal/payments")
class PaymentInternalController(
    private val paymentUseCase: PaymentUseCase,
) {
    @PostMapping("/authorize")
    fun authorizePayment(
        @RequestBody @Valid request: PaymentAuthorizeRequestDto,
    ): ResponseEntity<PaymentAuthorizeResponseDto> {
        val payment = paymentUseCase.authorizePayment(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentAuthorizeResponseDto.from(payment))
    }
}
