package com.playground.user.service

import com.playground.user.controller.response.UserResponseDto
import com.playground.user.domain.User
import com.playground.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository
) {

    fun signUp(username: String, password: String, nickname: String): UserResponseDto {
        val user = User(username = username, password = password, nickname = nickname)
        val savedUser = userRepository.save(user)
        return UserResponseDto.toResponse(savedUser)
    }
}