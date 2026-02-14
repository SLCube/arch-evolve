package com.playground.auth.presentation.response

data class AuthTokenResponseDto(
    val accessToken: String,
    val refreshToken: String,
) {
    override fun toString(): String {
        val maskedAccessToken = if (accessToken.length > 8) "${accessToken.substring(0, 8)}..." else "****"
        val maskedRefreshToken = if (refreshToken.length > 8) "${refreshToken.substring(0, 8)}..." else "****"
        return "AuthTokenResponseDto(accessToken='$maskedAccessToken', refreshToken='$maskedRefreshToken')"
    }
}
