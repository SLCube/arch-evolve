package com.playground.user.persistence.adapter

import com.playground.auth.application.port.out.AuthInfoPort
import com.playground.user.persistence.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class AuthInfoAdapter(
    private val userRepository: UserRepository,
) : AuthInfoPort {
    override fun getLoginIdById(userId: Long): String? =
        userRepository
            .findById(userId)
            .map { it.loginId }
            .orElse(null)
}
