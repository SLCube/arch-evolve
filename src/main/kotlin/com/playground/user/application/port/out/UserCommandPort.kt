package com.playground.user.application.port.out

import com.playground.user.domain.User

fun interface UserCommandPort {
    fun save(user: User): User
}