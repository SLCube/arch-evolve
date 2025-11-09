package com.playground.user.service

import com.playground.user.controller.response.UserResponseDto
import com.playground.user.domain.User
import com.playground.user.exception.DuplicateLoginIdException
import com.playground.user.exception.DuplicateNicknameException
import com.playground.user.exception.PasswordMismatchException
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
) {

    fun signUp(loginId: String, password: String, nickname: String): UserResponseDto {
        if (userRepository.existsByLoginId(loginId)) {
            throw DuplicateLoginIdException(loginId)
        }
        val encodePassword = passwordEncoder.encode(password)
        val user = User(loginId = loginId, password = encodePassword, nickname = nickname)
        val savedUser = userRepository.save(user)
        return UserResponseDto.toResponse(savedUser)
    }

    fun updateNickname(userId: Long, newNickname: String): UserResponseDto {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }

        if (user.nickname != newNickname) {
            userRepository.findByNickname(newNickname).ifPresent { foundUser ->
                if (foundUser.id != userId) {
                    throw DuplicateNicknameException()
                }
            }
        }

        user.updateNickname(newNickname)

        return UserResponseDto.toResponse(user)
    }

    fun updatePassword(userId: Long, oldPassword: String, newPassword: String): UserResponseDto {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }

        if (!passwordEncoder.matches(oldPassword, user.password)) {
            throw PasswordMismatchException()
        }

        val encodedNewPassword = passwordEncoder.encode(newPassword)
        user.updatePassword(encodedNewPassword)

        return UserResponseDto.toResponse(user)
    }
}