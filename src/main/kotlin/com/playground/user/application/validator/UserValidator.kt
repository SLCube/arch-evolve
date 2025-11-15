package com.playground.user.application.validator

import com.playground.user.application.port.out.UserQueryPort
import com.playground.user.domain.exception.DuplicateLoginIdException
import com.playground.user.domain.exception.DuplicateNicknameException
import com.playground.user.domain.exception.PasswordMismatchException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserValidator(
    private val userQueryPort: UserQueryPort,
    private val passwordEncoder: PasswordEncoder,
) {
    fun validateDuplicateLoginId(loginId: String) {
        userQueryPort.findByLoginId(loginId).ifPresent {
            throw DuplicateLoginIdException(loginId)
        }
    }

    fun validateDuplicateNickname(
        nickname: String,
        currentUserId: Long? = null,
    ) {
        userQueryPort.findByNickname(nickname).ifPresent { foundUser ->
            if (currentUserId == null || foundUser.id != currentUserId) {
                throw DuplicateNicknameException()
            }
        }
    }

    fun validateOldPassword(
        rawOldPassword: String,
        storedHashedPassword: String,
    ) {
        if (!passwordEncoder.matches(rawOldPassword, storedHashedPassword)) {
            throw PasswordMismatchException()
        }
    }
}
