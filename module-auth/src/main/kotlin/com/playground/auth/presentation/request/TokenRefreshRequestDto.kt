package com.playground.auth.presentation.request

import jakarta.validation.constraints.NotBlank

data class TokenRefreshRequestDto(
    @field:NotBlank(message = "Refresh Token은 필수입니다")
    val refreshToken: String,
)
