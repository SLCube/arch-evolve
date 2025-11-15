package com.playground.user.persistence.mapper

import com.playground.user.domain.model.User
import com.playground.user.persistence.entity.UserJpaEntity

fun UserJpaEntity.toDomain(): User =
    User(
        id = this.id,
        loginId = this.loginId,
        password = this.password,
        nickname = this.nickname,
        role = this.role,
    )
