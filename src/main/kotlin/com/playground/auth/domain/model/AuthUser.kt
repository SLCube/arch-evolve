package com.playground.auth.domain.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

data class AuthUser(
    val userId: Long,
    private val loginId: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>
): UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority?> = this.authorities

    override fun getPassword(): String = password

    override fun getUsername(): String = this.loginId

    override fun toString(): String {
        return "AuthUser(userId=$userId, loginId='$loginId', password='****', authorities=$authorities)"
    }
}