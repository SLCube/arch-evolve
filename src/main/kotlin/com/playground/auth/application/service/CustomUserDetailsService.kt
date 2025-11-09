package com.playground.auth.application.service

import com.playground.auth.application.port.out.UserLoadPort
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userLoadPort: UserLoadPort
): UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        return userLoadPort.loadUserByUsername(username)
    }
}