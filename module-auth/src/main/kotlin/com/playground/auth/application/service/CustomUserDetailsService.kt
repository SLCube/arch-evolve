package com.playground.auth.application.service

import com.playground.auth.contract.application.AuthUserQueryPort
import com.playground.auth.contract.security.AuthUserDetails
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val authUserQueryPort: AuthUserQueryPort
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = authUserQueryPort.getUserInfoByLoginId(username)

        return AuthUserDetails(
            userId = user.userId,
            loginId = user.loginId,
            password = user.password,
            roles = listOf(user.role),
        )
    }
}
