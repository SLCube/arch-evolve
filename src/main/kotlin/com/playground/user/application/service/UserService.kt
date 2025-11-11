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
import com.playground.user.domain.exception.UserNotFoundException
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
        userQueryPort.findByLoginId(command.loginId).ifPresent {
            throw DuplicateLoginIdException(command.loginId)
        }

        userQueryPort.findByNickname(command.nickname).ifPresent {
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
        userQueryPort.findByNickname(command.newNickname).ifPresent { foundUser ->
            if (foundUser.id != command.userId) {
                throw DuplicateNicknameException()
            }
        }

        val user = userQueryPort.findById(command.userId)
            .orElseThrow { UserNotFoundException() }

        user.updateNickname(command.newNickname)

        return userCommandPort.update(user)
    }

    override fun updatePassword(command: UpdatePasswordCommand): User {
        val user = userQueryPort.findById(command.userId)
            .orElseThrow { UserNotFoundException() }

        if (!passwordEncoder.matches(command.oldPassword, user.password)) {
            throw PasswordMismatchException()
        }

        user.updatePassword(passwordEncoder.encode(command.newPassword))

        return userCommandPort.update(user)
    }
}