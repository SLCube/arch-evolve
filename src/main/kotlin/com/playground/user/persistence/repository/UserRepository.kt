package com.playground.user.persistence.repository

import com.playground.user.persistence.entity.UserJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<UserJpaEntity, Long> {
    fun findByLoginId(loginId: String): Optional<UserJpaEntity>

    fun findByNickname(nickname: String): Optional<UserJpaEntity>

    fun findByNicknameAndIdNot(
        nickname: String,
        id: Long,
    ): Optional<UserJpaEntity>

    fun existsByLoginId(loginId: String): Boolean
}
