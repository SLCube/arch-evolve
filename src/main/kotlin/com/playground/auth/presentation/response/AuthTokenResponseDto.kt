package com.playground.auth.presentation.response

data class AuthTokenResponseDto(
    val accessToken: String,
) {
    override fun toString(): String {
        val maskedToken = if (accessToken.length > 8) "${accessToken.substring(0, 8)}..." else "****"
        return "AuthTokenResponseDto(accessToken='$maskedToken')"
    }
}