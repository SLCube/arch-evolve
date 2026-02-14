package com.playground.auth.presentation.request

import jakarta.validation.constraints.NotBlank

data class AuthLoginRequestDto(
    @field:NotBlank(message = "{user.loginId.not-blank}")
    val loginId: String,
    @field:NotBlank(message = "{user.password.not-blank}")
    val password: String,
) {
    override fun toString(): String = "AuthLoginRequestDto(loginId='$loginId', password='****')"
}
