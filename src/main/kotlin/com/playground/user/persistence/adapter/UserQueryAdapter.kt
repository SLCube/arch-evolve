package com.playground.user.persistence.adapter

import com.playground.user.application.port.out.UserQueryPort
import com.playground.user.domain.model.User
import com.playground.user.persistence.mapper.toDomain
import com.playground.user.persistence.repository.UserRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class UserQueryAdapter(
    private val userRepository: UserRepository
): UserQueryPort {
    override fun findById(userId: Long): Optional<User> {
        return userRepository.findById(userId)
            .map { it.toDomain() }
    }

    override fun findByLoginId(loginId: String): Optional<User> {
        return userRepository.findByLoginId(loginId)
            .map { it.toDomain() }
    }

    override fun findByNickname(nickname: String): Optional<User> {
        return userRepository.findByNickname(nickname)
            .map { it.toDomain() }
    }
}