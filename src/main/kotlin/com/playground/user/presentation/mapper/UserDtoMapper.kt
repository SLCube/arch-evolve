package com.playground.user.presentation.mapper

import com.playground.user.application.port.`in`.command.SignUpCommand
import com.playground.user.application.port.`in`.command.UpdateNicknameCommand
import com.playground.user.application.port.`in`.command.UpdatePasswordCommand
import com.playground.user.presentation.request.UserNicknameUpdateRequestDto
import com.playground.user.presentation.request.UserPasswordUpdateRequestDto
import com.playground.user.presentation.request.UserSignUpRequestDto

fun UserSignUpRequestDto.toCommand(): SignUpCommand {
    return SignUpCommand(
        loginId = this.loginId,
        password = this.password,
        nickname = this.nickname
    )
}

fun UserPasswordUpdateRequestDto.toCommand(userId: Long): UpdatePasswordCommand {
    return UpdatePasswordCommand(
        userId = userId,
        oldPassword = this.oldPassword,
        newPassword = this.newPassword
    )
}

fun UserNicknameUpdateRequestDto.toCommand(userId: Long): UpdateNicknameCommand {
    return UpdateNicknameCommand(
        userId = userId,
        newNickname = this.nickname
    )
}