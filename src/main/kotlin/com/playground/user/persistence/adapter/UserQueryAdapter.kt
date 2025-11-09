package com.playground.user.persistence.adapter

import com.playground.user.application.port.out.UserQueryPort
import com.playground.user.domain.User
import com.playground.user.domain.exception.UserNotFoundException
import com.playground.user.persistence.mapper.toDomain
import com.playground.user.persistence.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class UserQueryAdapter(
    private val userRepository: UserRepository
): UserQueryPort {
    override fun findById(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { UserNotFoundException() }
            .toDomain()
    }

    override fun existsByLoginId(loginId: String): Boolean {
        return userRepository.findByLoginId(loginId).isPresent
    }

    override fun existsByNickname(nickname: String): Boolean {
        return userRepository.findByNickname(nickname).isPresent
    }

    override fun existsByNicknameAndIdNot(nickname: String, userId: Long): Boolean {
        return userRepository.findByNicknameAndIdNot(nickname, userId).isPresent
    }
}