package com.playground.auth.presentation.controller

import com.playground.auth.application.port.inbound.LogoutUseCase
import com.playground.auth.contract.security.AuthUserDetails
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class LogoutController(
    private val logoutUseCase: LogoutUseCase,
) {
    @DeleteMapping("/logout")
    fun logout(
        @AuthenticationPrincipal authUserDetails: AuthUserDetails,
    ): ResponseEntity<Unit> {
        logoutUseCase.logout(authUserDetails.getUserId())
        return ResponseEntity.noContent().build()
    }
}
