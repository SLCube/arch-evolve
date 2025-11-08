package com.playground.user.controller

import com.playground.user.controller.request.UserNicknameUpdateRequestDto
import com.playground.user.controller.request.UserSignUpRequestDto
import com.playground.user.controller.response.UserResponseDto
import com.playground.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
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

    @PatchMapping("/{userId}/nickname")
    @PreAuthorize("@authChecker.isOwner(principal.username, #userId)")
    fun updateNickname(@PathVariable userId: Long, @RequestBody @Valid request: UserNicknameUpdateRequestDto): ResponseEntity<UserResponseDto> {
        val updatedUser = userService.updateNickname(userId, request.nickname)
        return ResponseEntity.ok(updatedUser)
    }
}