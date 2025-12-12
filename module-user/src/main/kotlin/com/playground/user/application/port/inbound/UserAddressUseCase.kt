package com.playground.user.application.port.inbound

import com.playground.user.application.port.inbound.command.AddressDefaultSetCommand
import com.playground.user.application.port.inbound.command.AddressRegisterCommand

interface UserAddressUseCase {
    fun registerAddress(command: AddressRegisterCommand)
    fun setDefaultAddress(command: AddressDefaultSetCommand)
}
