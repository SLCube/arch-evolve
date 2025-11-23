package com.playground.user.presentation.mapper

import com.playground.user.application.port.inbound.command.SignUpCommand
import com.playground.user.application.port.inbound.command.UpdateNicknameCommand
import com.playground.user.application.port.inbound.command.UpdatePasswordCommand
import com.playground.user.presentation.request.UserNicknameUpdateRequestDto
import com.playground.user.presentation.request.UserPasswordUpdateRequestDto
import com.playground.user.presentation.request.UserSignUpRequestDto

fun UserSignUpRequestDto.toCommand(): SignUpCommand =
    SignUpCommand(
        loginId = this.loginId,
        password = this.password,
        nickname = this.nickname,
    )

fun UserPasswordUpdateRequestDto.toCommand(requestUserId: Long, targetUserId: Long): UpdatePasswordCommand =
    UpdatePasswordCommand(
        requestUserId = requestUserId,
        targetUserId = targetUserId,
        oldPassword = this.oldPassword,
        newPassword = this.newPassword,
    )

fun UserNicknameUpdateRequestDto.toCommand(requestUserId: Long, targetUserId: Long): UpdateNicknameCommand =
    UpdateNicknameCommand(
        requestUserId = requestUserId,
        targetUserId = targetUserId,
        newNickname = this.nickname,
    )
