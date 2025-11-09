package com.playground.user.application.service

import com.playground.user.application.port.`in`.UserUseCase
import com.playground.user.application.port.`in`.command.SignUpCommand
import com.playground.user.application.port.`in`.command.UpdateNicknameCommand
import com.playground.user.application.port.`in`.command.UpdatePasswordCommand
import com.playground.user.application.port.out.UserCommandPort
import com.playground.user.application.port.out.UserQueryPort
import com.playground.user.domain.User
import com.playground.user.domain.exception.DuplicateLoginIdException
import com.playground.user.domain.exception.DuplicateNicknameException
import com.playground.user.domain.exception.PasswordMismatchException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userQueryPort: UserQueryPort,
    private val userCommandPort: UserCommandPort,
    private val passwordEncoder: PasswordEncoder,
): UserUseCase {

    override fun signUp(command: SignUpCommand): User {
        if (userQueryPort.existsByLoginId(command.loginId)) {
            throw DuplicateLoginIdException(command.loginId)
        }

        if (userQueryPort.existsByNickname(command.nickname)) {
            throw DuplicateNicknameException()
        }

        val user = User(
            loginId = command.loginId,
            password = passwordEncoder.encode(command.password),
            nickname = command.nickname
        )

        return userCommandPort.save(user)
    }

    override fun updateNickname(command: UpdateNicknameCommand): User {
        if (userQueryPort.existsByNicknameAndIdNot(command.newNickname, command.userId)) {
            throw DuplicateNicknameException()
        }

        val user = userQueryPort.findById(command.userId)

        user.updateNickname(command.newNickname)

        return userCommandPort.save(user)
    }

    override fun updatePassword(command: UpdatePasswordCommand): User {
        val user = userQueryPort.findById(command.userId)

        if (!passwordEncoder.matches(command.oldPassword, user.password)) {
            throw PasswordMismatchException()
        }

        user.updatePassword(passwordEncoder.encode(command.newPassword))

        return userCommandPort.save(user)
    }
}