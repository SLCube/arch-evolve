package com.playground.auth.application.service

import com.playground.auth.contract.security.AuthUserDetails
import com.playground.user.contract.application.port.outbound.UserInfoQueryPort
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userInfoQueryPort: UserInfoQueryPort
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val user = userInfoQueryPort.getUserInfoByLoginId(username)

        return AuthUserDetails(
            userId = user.userId,
            loginId = user.loginId,
            password = user.password,
            roles = listOf(user.role),
        )
    }
}
