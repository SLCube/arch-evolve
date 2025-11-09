package com.playground.common.security.service

import com.playground.user.domain.exception.UserNotFoundException
import com.playground.user.persistence.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
): UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByLoginId(username)
            .orElseThrow { UserNotFoundException() }

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))

        return User(user.loginId, user.password, authorities)
    }
}