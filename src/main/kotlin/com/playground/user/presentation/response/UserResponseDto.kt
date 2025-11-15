package com.playground.user.presentation.response

import com.playground.user.domain.model.User

data class UserResponseDto(
    val loginId: String,
    val nickname: String,
) {
    companion object {
        fun toResponse(domain: User): UserResponseDto = UserResponseDto(loginId = domain.loginId, nickname = domain.nickname)
    }
}
