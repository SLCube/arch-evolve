package com.playground.user.presentation.response

import com.playground.user.persistence.entity.User

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