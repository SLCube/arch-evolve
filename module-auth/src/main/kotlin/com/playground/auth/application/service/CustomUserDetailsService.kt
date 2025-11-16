package com.playground.auth.application.service

import com.playground.auth.application.security.AuthUserDetails
import com.playground.auth.domain.model.AuthUser
import com.playground.user.application.port.outbound.UserQueryPort
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userQueryPort: UserQueryPort,
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user =
            userQueryPort
                .findByLoginId(username)
                .orElseThrow { UsernameNotFoundException("User Not Found with loginId: $username") }

        val authUser =
            AuthUser(
                userId = user.id!!,
                loginId = user.loginId,
                password = user.password,
            )

        return AuthUserDetails(
            authUser = authUser,
            roles = listOf(user.role.name),
        )
    }
}
