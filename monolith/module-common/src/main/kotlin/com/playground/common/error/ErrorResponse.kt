package com.playground.common.error

data class ErrorResponse(
    val code: String,
    val message: String,
    val errors: Map<String, String?> = emptyMap(),
)
