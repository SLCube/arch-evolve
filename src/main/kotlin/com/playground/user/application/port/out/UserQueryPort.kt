package com.playground.user.application.port.out

import com.playground.user.domain.User

interface UserQueryPort {
    fun findById(userId: Long): User
    fun findByNickname(nickname: String): User
    fun existsByLoginId(loginId: String): Boolean
    fun existsByNickname(nickname: String): Boolean
}