package com.playground.user.persistence.adapter

import com.playground.auth.contract.application.AuthUserQueryPort
import com.playground.auth.contract.domain.vo.AuthUserInfo
import com.playground.user.persistence.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class AuthUserQueryAdapter(
    private val userRepository: UserRepository,
) : AuthUserQueryPort {
    override fun getUserInfoByLoginId(loginId: String): AuthUserInfo {
        val user = userRepository.findByLoginId(loginId).orElseThrow { UsernameNotFoundException("User Not Found with loginId: $loginId") }

        return AuthUserInfo(
            userId = user.id!!,
            loginId = user.loginId,
            password = user.password,
            role = user.role.name,
        )
    }
}