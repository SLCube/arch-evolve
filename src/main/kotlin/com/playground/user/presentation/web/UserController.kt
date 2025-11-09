package com.playground.user.presentation.web

import com.playground.common.security.annotation.CheckIsOwner
import com.playground.user.presentation.request.UserNicknameUpdateRequestDto
import com.playground.user.presentation.request.UserPasswordUpdateRequestDto
import com.playground.user.presentation.request.UserSignUpRequestDto
import com.playground.user.presentation.response.UserResponseDto
import com.playground.user.application.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping("/sign-up")
    fun signUp(@RequestBody @Valid request: UserSignUpRequestDto): ResponseEntity<UserResponseDto> {
        val savedUser = userService.signUp(request.loginId, request.password, request.nickname)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser)
    }

    @CheckIsOwner
    @PatchMapping("/{userId}/nickname")
    fun updateNickname(@PathVariable userId: Long, @RequestBody @Valid request: UserNicknameUpdateRequestDto): ResponseEntity<UserResponseDto> {
        val updatedUser = userService.updateNickname(userId, request.nickname)
        return ResponseEntity.ok(updatedUser)
    }

    @CheckIsOwner
    @PatchMapping("/{userId}/password")
    fun updatePassword(@PathVariable userId: Long, @RequestBody @Valid request: UserPasswordUpdateRequestDto): ResponseEntity<UserResponseDto> {
        val updatedUser = userService.updatePassword(userId, request.oldPassword, request.newPassword)
        return ResponseEntity.ok(updatedUser)
    }
}