package com.playground.user.controller.response

import com.playground.user.domain.User

data class UserResponseDto(
    val username: String,
    val nickname: String,
) {
    companion object {
        fun toResponse(user: User): UserResponseDto {
            return UserResponseDto(username = user.username, nickname = user.nickname)
        }
    }
}
