package com.playground.user.application.port.inbound.command

data class UpdateNicknameCommand(
    val userId: Long,
    val newNickname: String,
)
