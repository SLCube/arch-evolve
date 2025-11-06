package com.playground.user.controller.request

import jakarta.validation.constraints.NotBlank

data class UserLoginRequestDto(
    @field:NotBlank(message = "{user.loginId.not-blank}")
    val loginId: String,
    @field:NotBlank(message = "{user.password.not-blank}")
    val password: String,
)
