package com.playground.user.presentation.web

import com.playground.user.application.port.inbound.UserAddressUseCase
import org.springframework.web.bind.annotation.RestController

@RestController("/users/addresses")
class UserAddressController(
    private val userAddressUseCase: UserAddressUseCase,
) {

}