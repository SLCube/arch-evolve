package com.playground.user.application.port.inbound.command

data class SignUpCommand(
    val loginId: String,
    val password: String,
    val nickname: String,
)
