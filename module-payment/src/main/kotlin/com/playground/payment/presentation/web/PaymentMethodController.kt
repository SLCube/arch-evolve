package com.playground.payment.presentation.web

import com.playground.auth.contract.security.AuthUserDetails
import com.playground.payment.application.port.inbound.PaymentMethodUseCase
import com.playground.payment.application.port.inbound.command.PaymentMethodDeleteCommand
import com.playground.payment.presentation.mapper.toCommand
import com.playground.payment.presentation.request.PaymentMethodRegisterRequestDto
import com.playground.payment.presentation.response.PaymentMethodResponseDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/payment-methods")
class PaymentMethodController(
    private val paymentMethodUseCase: PaymentMethodUseCase,
) {

    @PostMapping
    fun registerPaymentMethod(
        @AuthenticationPrincipal authUserDetails: AuthUserDetails,
        @RequestBody @Valid request: PaymentMethodRegisterRequestDto,
    ): ResponseEntity<PaymentMethodResponseDto> {
        val command = request.toCommand(authUserDetails.getUserId())
        val createdPaymentMethod = paymentMethodUseCase.registerPaymentMethod(command)

        return ResponseEntity.status(HttpStatus.CREATED).body(PaymentMethodResponseDto.toResponse(createdPaymentMethod))
    }

    @GetMapping
    fun getPaymentMethodList(
        @AuthenticationPrincipal authUserDetails: AuthUserDetails,
    ): ResponseEntity<List<PaymentMethodResponseDto>> {
        val paymentMethodList = paymentMethodUseCase.getPaymentMethodList(authUserDetails.getUserId())
        val response = paymentMethodList.map { PaymentMethodResponseDto.toResponse(it) }

        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @DeleteMapping("/{paymentMethodId}")
    fun deletePaymentMethod(
        @AuthenticationPrincipal authUserDetails: AuthUserDetails,
        @PathVariable paymentMethodId: Long,
    ): ResponseEntity<Unit> {
        val command = PaymentMethodDeleteCommand(
            userId = authUserDetails.getUserId(),
            paymentMethodId = paymentMethodId,
        )

        paymentMethodUseCase.deletePaymentMethod(command)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }
}