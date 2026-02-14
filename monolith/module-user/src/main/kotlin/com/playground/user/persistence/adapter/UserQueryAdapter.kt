package com.playground.user.persistence.adapter

import com.playground.user.application.port.outbound.UserQueryPort
import com.playground.user.domain.model.User
import com.playground.user.persistence.mapper.toDomain
import com.playground.user.persistence.repository.UserRepository
import org.springframework.stereotype.Component
import java.util.Optional

@Component
class UserQueryAdapter(
    private val userRepository: UserRepository,
) : UserQueryPort {
    override fun findById(userId: Long): Optional<User> = userRepository.findById(userId).map { it.toDomain() }

    override fun findByLoginId(loginId: String): Optional<User> =
        userRepository.findByLoginId(loginId).map { it.toDomain() }

    override fun findByNickname(nickname: String): Optional<User> =
        userRepository.findByNickname(nickname).map { it.toDomain() }

    override fun findUserWithAddressById(userId: Long): Optional<User> =
        userRepository.findWithAddressById(userId).map { it.toDomain() }

}
