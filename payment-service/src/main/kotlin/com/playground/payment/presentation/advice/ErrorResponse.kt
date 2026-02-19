package com.playground.payment.presentation.advice

data class ErrorResponse(
    val code: String,
    val message: String,
    val errors: Map<String, String?> = emptyMap(),
)
