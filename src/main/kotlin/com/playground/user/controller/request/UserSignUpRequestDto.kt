package com.playground.user.controller.request

data class UserSignUpRequestDto(
    val username: String,
    val password: String,
    val nickname: String,
)
