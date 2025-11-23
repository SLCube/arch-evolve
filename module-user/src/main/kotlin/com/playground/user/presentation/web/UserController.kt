package com.playground.user.presentation.web

import com.playground.auth.contract.security.AuthUserDetails
import com.playground.user.application.port.inbound.UserUseCase
import com.playground.user.presentation.mapper.toCommand
import com.playground.user.presentation.request.UserNicknameUpdateRequestDto
import com.playground.user.presentation.request.UserPasswordUpdateRequestDto
import com.playground.user.presentation.request.UserSignUpRequestDto
import com.playground.user.presentation.response.UserResponseDto
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userUseCase: UserUseCase,
) {
    @PostMapping("/sign-up")
    fun signUp(
        @RequestBody @Valid request: UserSignUpRequestDto,
    ): ResponseEntity<UserResponseDto> {
        val savedUser = userUseCase.signUp(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponseDto.toResponse(savedUser))
    }

    @PatchMapping("/{userId}/nickname")
    fun updateNickname(
        @PathVariable userId: Long,
        @AuthenticationPrincipal userDetails: AuthUserDetails,
        @RequestBody @Valid request: UserNicknameUpdateRequestDto,
    ): ResponseEntity<UserResponseDto> {
        val updatedUser = userUseCase.updateNickname(
            request.toCommand(
                requestUserId = userId,
                targetUserId = userDetails.getUserId(),
            )
        )
        return ResponseEntity.ok(UserResponseDto.toResponse(updatedUser))
    }

    @PatchMapping("/{userId}/password")
    fun updatePassword(
        @PathVariable userId: Long,
        @AuthenticationPrincipal userDetails: AuthUserDetails,
        @RequestBody @Valid request: UserPasswordUpdateRequestDto,
    ): ResponseEntity<UserResponseDto> {
        val updatedUser = userUseCase.updatePassword(
            request.toCommand(
                requestUserId = userId,
                targetUserId = userDetails.getUserId(),
            )
        )
        return ResponseEntity.ok(UserResponseDto.toResponse(updatedUser))
    }
}
