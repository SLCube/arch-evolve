package com.playground.user.presentation.request

import com.playground.common.constant.ValidationConstants
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserSignUpRequestDto(
    @field:NotBlank(message = "{user.loginId.not-blank}")
    @field:Size(min = ValidationConstants.USER_LOGIN_ID_MIN_SIZE, max = ValidationConstants.USER_LOGIN_ID_MAX_SIZE, message = "{user.loginId.size}")
    val loginId: String,
    @field:NotBlank(message = "{user.password.not-blank}")
    @field:Size(min = ValidationConstants.USER_PASSWORD_MIN_SIZE, max = ValidationConstants.USER_PASSWORD_MAX_SIZE, message = "{user.password.size}")
    val password: String,
    @field:NotBlank(message = "{user.nickname.not-blank}")
    @field:Size(min = ValidationConstants.USER_NICKNAME_MIN_SIZE, max = ValidationConstants.USER_NICKNAME_MAX_SIZE, message = "{user.nickname.size}")
    val nickname: String,
) {
    override fun toString(): String {
        return "UserSignUpRequestDto(loginId='$loginId', password='****', nickname='$nickname')"
    }
}