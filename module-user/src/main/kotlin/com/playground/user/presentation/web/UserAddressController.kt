package com.playground.user.presentation.web

import com.playground.auth.contract.security.AuthUserDetails
import com.playground.user.application.port.inbound.UserAddressUseCase
import com.playground.user.presentation.mapper.toAddressDefaultSetCommand
import com.playground.user.presentation.mapper.toCommand
import com.playground.user.presentation.request.AddressRegisterRequestDto
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
@RequestMapping("/users/addresses")
class UserAddressController(
    private val userAddressUseCase: UserAddressUseCase,
) {
    @PostMapping
    fun registerAddress(
        @AuthenticationPrincipal userDetails: AuthUserDetails,
        @RequestBody @Valid request: AddressRegisterRequestDto,
    ): ResponseEntity<Unit> {
        userAddressUseCase.registerAddress(
            request.toCommand(
                userId = userDetails.getUserId(),
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @PatchMapping("/{addressId}/default")
    fun setDefaultAddress(
        @AuthenticationPrincipal userDetails: AuthUserDetails,
        @PathVariable addressId: Long,
    ): ResponseEntity<Unit> {
        userAddressUseCase.setDefaultAddress(
            toAddressDefaultSetCommand(
                userId = userDetails.getUserId(),
                addressId = addressId,
            )
        )
        return ResponseEntity.noContent().build()
    }
}
