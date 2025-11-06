package com.playground.user.controller

import com.playground.user.controller.request.UserLoginRequestDto
import com.playground.user.controller.request.UserSignUpRequestDto
import com.playground.user.controller.response.UserLoginResponseDto
import com.playground.user.controller.response.UserResponseDto
import com.playground.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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

    @PostMapping("/login")
    fun login(@RequestBody @Valid request: UserLoginRequestDto): ResponseEntity<UserLoginResponseDto> {
        val loginResponse = userService.login(request.loginId, request.password)
        return ResponseEntity.ok().body(loginResponse)
    }
}