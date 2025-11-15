package com.playground.user.persistence.adapter

import com.playground.user.application.port.out.UserCommandPort
import com.playground.user.domain.exception.UserNotFoundException
import com.playground.user.domain.model.User
import com.playground.user.persistence.entity.UserJpaEntity
import com.playground.user.persistence.mapper.toDomain
import com.playground.user.persistence.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class UserCommandAdapter(
    private val userRepository: UserRepository,
) : UserCommandPort {
    override fun save(user: User): User {
        val userJpaEntity = UserJpaEntity.toJpaEntity(user)
        val savedEntity = userRepository.save(userJpaEntity)
        return savedEntity.toDomain()
    }

    override fun update(user: User): User {
        val userId = requireNotNull(user.id) { "User ID must not be null for update" }

        val userJpaEntity =
            userRepository
                .findById(userId)
                .orElseThrow { UserNotFoundException() }

        userJpaEntity.update(
            loginId = user.loginId,
            password = user.password,
            nickname = user.nickname,
        )

        return userJpaEntity.toDomain()
    }
}
