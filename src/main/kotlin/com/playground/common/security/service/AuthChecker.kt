package com.playground.common.security.service

import com.playground.user.persistence.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class AuthChecker(
    private val userRepository: UserRepository
) {
    fun isOwner(username: String, userId: Long): Boolean {
        val user = userRepository.findById(userId).orElse(null) ?: return false
        return user.loginId == username
    }
}