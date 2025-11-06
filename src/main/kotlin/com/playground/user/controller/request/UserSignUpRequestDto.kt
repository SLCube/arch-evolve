package com.playground.user.controller.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserSignUpRequestDto(
    @field:Size(min = 4, max = 20, message = "{user.loginId.size}")
    val loginId: String,
    @field:Size(min = 8, max = 16, message = "{user.password.size}")
    val password: String,
    @field:NotBlank(message = "{user.nickname.not-blank}")
    val nickname: String,
)
