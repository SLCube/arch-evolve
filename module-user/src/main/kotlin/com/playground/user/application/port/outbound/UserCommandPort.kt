package com.playground.user.application.port.outbound

import com.playground.user.domain.model.User

interface UserCommandPort {
    fun save(user: User): com.playground.user.domain.model.User

    fun update(user: User): com.playground.user.domain.model.User
}
