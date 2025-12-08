package com.playground.user.fixture.application.command

import com.playground.user.application.port.inbound.command.SignUpCommand
import com.playground.user.application.port.inbound.command.UpdateNicknameCommand
import com.playground.user.application.port.inbound.command.UpdatePasswordCommand

object UserCommandTestFixture {

    fun signUpCommand(
        loginId: String = "new-user",
        password: String = "Password#1",
        nickname: String = "가입유저",
    ) = SignUpCommand(
        loginId = loginId,
        password = password,
        nickname = nickname,
    )

    fun updateNicknameCommand(
        userId: Long = 1L,
        newNickname: String = "새로운닉네임",
    ) = UpdateNicknameCommand(
        userId = userId,
        newNickname = newNickname,
    )

    fun updatePasswordCommand(
        userId: Long = 1L,
        oldPassword: String = "old-password",
        newPassword: String = "new-password",
    ) = UpdatePasswordCommand(
        userId = userId,
        oldPassword = oldPassword,
        newPassword = newPassword,
    )
}
