package com.playground.user.service

import com.playground.common.security.jwt.JwtTokenProvider
import com.playground.user.controller.response.UserLoginResponseDto
import com.playground.user.controller.response.UserResponseDto
import com.playground.user.domain.User
import com.playground.user.exception.InValidPasswordException
import com.playground.user.exception.UserNotFoundException
import com.playground.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun signUp(loginId: String, password: String, nickname: String): UserResponseDto {
        val encodePassword = passwordEncoder.encode(password)
        val user = User(loginId = loginId, password = encodePassword, nickname = nickname)
        val savedUser = userRepository.save(user)
        return UserResponseDto.toResponse(savedUser)
    }

    fun login(loginId: String, password: String): UserLoginResponseDto {
        val foundUser = userRepository.findByLoginId(loginId).orElseThrow { UserNotFoundException() }

        if (!passwordEncoder.matches(password, foundUser.password)) {
            throw InValidPasswordException()
        }

        val accessToken = jwtTokenProvider.generateToken(foundUser.loginId)

        return UserLoginResponseDto(accessToken)
    }
}