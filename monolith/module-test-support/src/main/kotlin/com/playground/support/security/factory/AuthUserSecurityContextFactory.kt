package com.playground.support.security.factory

import com.playground.auth.contract.security.AuthUserDetails
import com.playground.support.security.annotation.WithMockAuthUser
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.util.Collections

class AuthUserSecurityContextFactory: WithSecurityContextFactory<WithMockAuthUser> {
    override fun createSecurityContext(currentUser: WithMockAuthUser): SecurityContext {
        val authUserPrincipal = AuthUserDetails(
            userId = currentUser.userId,
            loginId = currentUser.loginId,
            password = currentUser.password,
            roles = listOf(currentUser.role)
        )

        val authorities = Collections.singletonList(
            SimpleGrantedAuthority("ROLE_${currentUser.role}")
        )

        val auth: Authentication = UsernamePasswordAuthenticationToken(
            authUserPrincipal,
            null,
            authorities
        )

        val context: SecurityContext = SecurityContextHolder.createEmptyContext()
        context.authentication = auth

        return context

    }
}