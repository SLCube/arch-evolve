package com.playground.user.persistence.adapter

import com.playground.user.application.port.out.UserCommandPort
import com.playground.user.domain.User
import com.playground.user.persistence.entity.UserJpaEntity
import com.playground.user.persistence.mapper.toDomain
import com.playground.user.persistence.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class UserCommandAdapter(
    private val userRepository: UserRepository
): UserCommandPort {
    override fun save(user: User): User {
        val userJpaEntity = UserJpaEntity.toJpaEntity(user)
        val savedEntity = userRepository.save(userJpaEntity)
        return savedEntity.toDomain()
    }
}