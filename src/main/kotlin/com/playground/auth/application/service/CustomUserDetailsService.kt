package com.playground.auth.application.service

import com.playground.auth.domain.model.AuthUser
import com.playground.user.application.port.out.UserQueryPort
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userQueryPort: UserQueryPort
): UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userQueryPort.findByLoginId(username)
            .orElseThrow { UsernameNotFoundException("User Not Found with loginId: $username") }

        return AuthUser(
            userId = user.id!!,
            loginId = user.loginId,
            authorities = setOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
        )
    }
}