package com.playground.user.application.port.inbound

import com.playground.user.application.port.inbound.command.AddressDefaultSetCommand
import com.playground.user.application.port.inbound.command.AddressRegisterCommand
import com.playground.user.domain.model.UserAddress

interface UserAddressUseCase {
    fun registerAddress(command: AddressRegisterCommand) : UserAddress
    fun setDefaultAddress(command: AddressDefaultSetCommand)
}