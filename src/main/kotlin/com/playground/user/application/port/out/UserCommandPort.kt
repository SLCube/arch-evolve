package com.playground.user.application.port.out

import com.playground.user.domain.User

interface UserCommandPort {
    fun save(user: User): User
    fun update(user: User): User
}