package com.playground.user.application.validator

import com.playground.auth.contract.application.port.outbound.PasswordEncoderPort
import com.playground.user.application.port.outbound.UserQueryPort
import com.playground.user.domain.exception.DuplicateLoginIdException
import com.playground.user.domain.exception.DuplicateNicknameException
import com.playground.user.domain.exception.PasswordMismatchException
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component

@Component
class UserValidator(
    private val userQueryPort: UserQueryPort,
    private val passwordEncoderPort: PasswordEncoderPort,
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
        if (!passwordEncoderPort.matches(rawOldPassword, storedHashedPassword)) {
            throw PasswordMismatchException()
        }
    }

    fun validateOwnership(requestUserId: Long, targetUserId: Long) {
        if (requestUserId != targetUserId) {
            throw AccessDeniedException("접근 권한이 없습니다.")
        }
    }
}
