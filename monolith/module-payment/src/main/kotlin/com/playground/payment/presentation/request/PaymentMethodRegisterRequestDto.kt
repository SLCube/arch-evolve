package com.playground.payment.presentation.request

import jakarta.validation.constraints.NotBlank

data class PaymentMethodRegisterRequestDto(
    @field:NotBlank
    val authKey: String,
    @field:NotBlank
    val cardCompany: String,
    @field:NotBlank
    val cardNumberMasked: String,
    val setAsDefault: Boolean,
)
