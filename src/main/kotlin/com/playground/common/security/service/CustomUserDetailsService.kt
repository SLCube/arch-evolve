package com.playground.common.security.service

import com.playground.user.exception.UserNotFoundException
import com.playground.user.repository.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
): UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails? {
        val user = userRepository.findByLoginId(username)
            .orElseThrow { UserNotFoundException() }

        // TODO: roles 추가 예정
        return User(user.loginId, user.password, emptyList())
    }
}