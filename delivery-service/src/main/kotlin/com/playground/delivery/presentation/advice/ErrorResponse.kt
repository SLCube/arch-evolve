package com.playground.delivery.presentation.advice

data class ErrorResponse(
    val code: String,
    val message: String,
    val errors: Map<String, String?> = emptyMap(),
)
