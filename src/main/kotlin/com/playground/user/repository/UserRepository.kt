package com.playground.user.repository

import com.playground.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository: JpaRepository<User, Long> {
    fun findByLoginId(loginId: String): Optional<User>
    fun findByNickname(nickname: String): Optional<User>
    fun existsByLoginId(loginId: String): Boolean
}