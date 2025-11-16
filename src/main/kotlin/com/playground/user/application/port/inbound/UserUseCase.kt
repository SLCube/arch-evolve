package com.playground.user.application.port.inbound

import com.playground.user.application.port.inbound.command.SignUpCommand
import com.playground.user.application.port.inbound.command.UpdateNicknameCommand
import com.playground.user.application.port.inbound.command.UpdatePasswordCommand
import com.playground.user.domain.model.User

interface UserUseCase {
    fun signUp(command: SignUpCommand): User

    fun updateNickname(command: UpdateNicknameCommand): User

    fun updatePassword(command: UpdatePasswordCommand): User
}
