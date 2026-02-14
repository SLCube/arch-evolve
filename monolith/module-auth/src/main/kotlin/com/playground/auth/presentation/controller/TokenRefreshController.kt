package com.playground.auth.presentation.controller

import com.playground.auth.application.port.inbound.TokenRefreshUseCase
import com.playground.auth.presentation.request.TokenRefreshRequestDto
import com.playground.auth.presentation.response.AuthTokenResponseDto
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class TokenRefreshController(
    private val tokenRefreshUseCase: TokenRefreshUseCase,
) {
    @PostMapping("/refresh")
    fun refreshToken(
        @Valid @RequestBody request: TokenRefreshRequestDto,
    ): ResponseEntity<AuthTokenResponseDto> {
        val response = tokenRefreshUseCase.refresh(request.refreshToken)
        return ResponseEntity.ok(response)
    }
}
