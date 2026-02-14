package com.playground.user.application.port.outbound

import com.playground.user.domain.model.User

interface UserCommandPort {
    fun save(user: User): User

    fun update(user: User): User
}
