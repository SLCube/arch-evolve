package com.playground.user.persistence.adapter

import com.playground.auth.application.port.out.AuthInfoPort
import com.playground.auth.application.port.out.UserLoadPort
import com.playground.user.domain.exception.UserNotFoundException
import com.playground.user.persistence.repository.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component

@Component
class UserLoadAdapter(
    private val userRepository: UserRepository,
) : UserLoadPort,
    AuthInfoPort {
    override fun loadUserByUsername(username: String): UserDetails {
        val userJpaEntity =
            userRepository
                .findByLoginId(username)
                .orElseThrow { UserNotFoundException() }

        val authorities = listOf(SimpleGrantedAuthority("ROLE_${userJpaEntity.role.name}"))

        return User(userJpaEntity.loginId, userJpaEntity.password, authorities)
    }

    override fun getLoginIdById(userId: Long): String? =
        userRepository
            .findById(userId)
            .map { it.loginId }
            .orElse(null)
}
