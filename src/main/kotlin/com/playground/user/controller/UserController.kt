package com.playground.user.controller

import com.playground.user.controller.request.UserLoginRequestDto
import com.playground.user.controller.request.UserSignUpRequestDto
import com.playground.user.controller.response.UserResponseDto
import com.playground.user.service.UserService
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
    fun signUp(@RequestBody request: UserSignUpRequestDto): ResponseEntity<UserResponseDto> {
        val savedUser = userService.signUp(request.username, request.password, request.nickname)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: UserLoginRequestDto): ResponseEntity<Unit> {
        userService.login(request.username, request.password)
        return ResponseEntity.ok().build()
    }
}