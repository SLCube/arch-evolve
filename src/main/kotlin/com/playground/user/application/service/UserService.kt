package com.playground.user.application.service

import com.playground.user.application.port.`in`.UserUseCase
import com.playground.user.application.port.`in`.command.SignUpCommand
import com.playground.user.application.port.`in`.command.UpdateNicknameCommand
import com.playground.user.application.port.`in`.command.UpdatePasswordCommand
import com.playground.user.application.port.out.UserCommandPort
import com.playground.user.application.port.out.UserEventPort
import com.playground.user.application.port.out.UserQueryPort
import com.playground.user.application.validator.UserValidator
import com.playground.user.domain.event.UserNicknameUpdatedEvent
import com.playground.user.domain.event.UserPasswordUpdatedEvent
import com.playground.user.domain.event.UserSignedUpEvent
import com.playground.user.domain.exception.UserNotFoundException
import com.playground.user.domain.model.User
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userQueryPort: UserQueryPort,
    private val userCommandPort: UserCommandPort,
    private val userEventPort: UserEventPort,
    private val userValidator: UserValidator,
    private val passwordEncoder: PasswordEncoder,
) : UserUseCase {
    override fun signUp(command: SignUpCommand): User {
        userValidator.validateDuplicateLoginId(command.loginId)
        userValidator.validateDuplicateNickname(command.nickname)

        val user =
            User(
                loginId = command.loginId,
                password = passwordEncoder.encode(command.password),
                nickname = command.nickname,
            )

        val savedUser = userCommandPort.save(user)

        userEventPort.publish(
            UserSignedUpEvent(
                userId = savedUser.id!!,
                loginId = savedUser.loginId,
            ),
        )

        return savedUser
    }

    override fun updateNickname(command: UpdateNicknameCommand): User {
        userValidator.validateDuplicateNickname(command.newNickname, command.userId)

        val user = findUserById(command.userId)

        val oldNickname = user.nickname
        user.updateNickname(command.newNickname)

        val updatedUser = userCommandPort.update(user)

        userEventPort.publish(
            UserNicknameUpdatedEvent(
                userId = updatedUser.id!!,
                loginId = updatedUser.loginId,
                oldNickname = oldNickname,
                newNickname = updatedUser.nickname,
            ),
        )

        return updatedUser
    }

    override fun updatePassword(command: UpdatePasswordCommand): User {
        val user = findUserById(command.userId)

        userValidator.validateOldPassword(command.oldPassword, user.password)

        user.updatePassword(passwordEncoder.encode(command.newPassword))

        val updatedUser = userCommandPort.update(user)

        userEventPort.publish(
            UserPasswordUpdatedEvent(
                userId = updatedUser.id!!,
                loginId = updatedUser.loginId,
            ),
        )

        return updatedUser
    }

    private fun findUserById(userId: Long): User =
        userQueryPort
            .findById(userId)
            .orElseThrow { UserNotFoundException() }
}
