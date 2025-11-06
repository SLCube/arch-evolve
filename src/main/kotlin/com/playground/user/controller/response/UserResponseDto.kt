package com.playground.user.controller.response

import com.playground.user.domain.User

data class UserResponseDto(
    val loginId: String,
    val nickname: String,
) {
    companion object {
        fun toResponse(user: User): UserResponseDto {
            return UserResponseDto(loginId = user.loginId, nickname = user.nickname)
        }
    }
}
