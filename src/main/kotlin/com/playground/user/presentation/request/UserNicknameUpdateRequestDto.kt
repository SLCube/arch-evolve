package com.playground.user.presentation.request

import com.playground.common.constant.ValidationConstants
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserNicknameUpdateRequestDto(
    @field:NotBlank(message = "{user.nickname.not-blank}")
    @field:Size(min = ValidationConstants.USER_NICKNAME_MIN_SIZE, max = ValidationConstants.USER_NICKNAME_MAX_SIZE, message = "{user.nickname.size}")
    val nickname: String
)
