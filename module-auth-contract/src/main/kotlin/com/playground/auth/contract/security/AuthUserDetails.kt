package com.playground.auth.contract.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class AuthUserDetails(
    private val userId: Long,
    private val loginId: String,
    private val password: String,
    private val roles: List<String>,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> = roles.map { SimpleGrantedAuthority("ROLE_$it") }

    override fun getPassword(): String = password

    override fun getUsername(): String = loginId

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    fun getUserId(): Long = userId
}