package com.playground.user.application.port.out

import com.playground.user.domain.model.User
import java.util.Optional

interface UserQueryPort {
    fun findById(userId: Long): Optional<User>

    fun findByLoginId(loginId: String): Optional<User>

    fun findByNickname(nickname: String): Optional<User>
}
