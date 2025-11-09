package com.playground.auth.application.port.out

import org.springframework.security.core.userdetails.UserDetails

fun interface UserLoadPort {
    fun loadUserByUsername(username: String): UserDetails
}