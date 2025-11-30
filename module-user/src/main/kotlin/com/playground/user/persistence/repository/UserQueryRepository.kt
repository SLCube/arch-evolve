package com.playground.user.persistence.repository

import com.playground.user.persistence.entity.UserJpaEntity
import java.util.Optional

fun interface UserQueryRepository {
    fun findWithAddressById(userId: Long): Optional<UserJpaEntity>
}