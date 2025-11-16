package com.playground.auth.application.security

import com.playground.auth.domain.model.AuthUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AuthUserDetails(
    private val authUser: AuthUser,
    private val roles: List<String>,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = roles.map { SimpleGrantedAuthority("ROLE_$it") }

    override fun getPassword(): String = authUser.password

    override fun getUsername(): String = authUser.loginId

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    fun getUserId(): Long = authUser.userId
}
