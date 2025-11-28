package com.playground.user.persistence.adapter

import com.playground.user.contract.application.port.outbound.UserInfoQueryPort
import com.playground.user.contract.domain.vo.UserInfo
import com.playground.user.persistence.repository.UserRepository
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component

@Component
class UserInfoQueryAdapter(
    private val userRepository: UserRepository,
) : UserInfoQueryPort {
    override fun getUserInfoByLoginId(loginId: String): UserInfo {
        val user = userRepository.findByLoginId(loginId).orElseThrow { UsernameNotFoundException("User Not Found with loginId: $loginId") }

        return UserInfo(
            userId = user.id!!,
            loginId = user.loginId,
            password = user.password,
            role = user.role.name,
        )
    }
}