package com.playground.user.application.port.inbound.command

data class UpdateNicknameCommand(
    val requestUserId: Long,
    val targetUserId: Long,
    val newNickname: String,
)
