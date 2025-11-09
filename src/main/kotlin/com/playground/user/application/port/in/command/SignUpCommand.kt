package com.playground.user.application.port.`in`.command

data class SignUpCommand(
    val loginId: String,
    val password: String,
    val nickname: String,
)
