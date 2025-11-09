package com.playground.user.presentation.request

import com.playground.common.constant.ValidationConstants
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UserPasswordUpdateRequestDto(
    @field:NotBlank(message = "기존 비밀번호는 비어있을 수 없습니다.")
    val oldPassword: String,

    @field:NotBlank(message = "새 비밀번호는 비어있을 수 없습니다.")
    @field:Size(min = ValidationConstants.USER_PASSWORD_MIN_SIZE, max = ValidationConstants.USER_PASSWORD_MAX_SIZE, message = "새 비밀번호는 8자 이상 16자 이하로 입력해주세요.")
    val newPassword: String
)
