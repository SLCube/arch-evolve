package com.playground.user.application.port.`in`

import com.playground.user.application.port.`in`.command.SignUpCommand
import com.playground.user.application.port.`in`.command.UpdateNicknameCommand
import com.playground.user.application.port.`in`.command.UpdatePasswordCommand
import com.playground.user.domain.User

interface UserUseCase {
    fun signUp(command: SignUpCommand): User
    fun updateNickname(command: UpdateNicknameCommand): User
    fun updatePassword(command: UpdatePasswordCommand): User
}