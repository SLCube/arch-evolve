package com.playground.user.application.port.`in`.command

data class UpdateNicknameCommand(
    val userId: Long,
    val newNickname: String,
)
